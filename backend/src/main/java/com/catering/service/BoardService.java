package com.catering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.catering.entity.Dish;
import com.catering.common.BusinessException;
import com.catering.mapper.DishMapper;
import com.catering.task.BoardCalculationTask;
import com.catering.vo.DishVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BoardService {

    private static final BigDecimal RED_SCORE_MIN = new BigDecimal("4.5");
    private static final BigDecimal BLACK_SCORE_MAX = new BigDecimal("2.5");
    private static final int BOARD_LIMIT = 10;

    private final DishMapper dishMapper;
    private final BoardCalculationTask boardCalculationTask;

    public BoardService(DishMapper dishMapper, BoardCalculationTask boardCalculationTask) {
        this.dishMapper = dishMapper;
        this.boardCalculationTask = boardCalculationTask;
    }

    public Map<String, Object> getAdminBoardList() {
        List<Dish> dishes = loadActiveDishes();
        List<Map<String, Object>> red = buildBoardEntries(dishes, true, true);
        List<Map<String, Object>> black = buildBoardEntries(dishes, false, true);
        Map<String, Object> result = new HashMap<>();
        result.put("redList", red);
        result.put("blackList", black);
        return result;
    }

    public Map<String, List<DishVO>> getPublicRedBlack() {
        List<Dish> dishes = loadActiveDishes();
        List<DishVO> red = buildBoardEntries(dishes, true, false).stream()
                .map(this::mapToDishVO)
                .collect(Collectors.toList());
        List<DishVO> black = buildBoardEntries(dishes, false, false).stream()
                .map(this::mapToDishVO)
                .collect(Collectors.toList());
        Map<String, List<DishVO>> map = new HashMap<>();
        map.put("red", red);
        map.put("black", black);
        return map;
    }

    public void interveneBoard(Long dishId, String action) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        switch (action) {
            case "add_red":
                dish.setBoardStatus("red");
                dish.setBoardHidden(0);
                break;
            case "add_black":
                dish.setBoardStatus("black");
                dish.setBoardHidden(0);
                break;
            case "remove_red":
                updateDishFields(dishId, uw -> uw
                        .set(Dish::getBoardStatus, "red_remove")
                        .set(Dish::getBoardSort, null));
                return;
            case "remove_black":
                updateDishFields(dishId, uw -> uw
                        .set(Dish::getBoardStatus, "black_remove")
                        .set(Dish::getBoardSort, null));
                return;
            case "cancel":
                updateDishFields(dishId, uw -> uw
                        .set(Dish::getBoardStatus, null)
                        .set(Dish::getBoardSort, null)
                        .set(Dish::getBoardHidden, 0));
                return;
            case "hide":
                dish.setBoardHidden(1);
                break;
            case "show":
                dish.setBoardHidden(0);
                break;
            case "pin_red":
                pinOnBoard(dish, true);
                return;
            case "pin_black":
                pinOnBoard(dish, false);
                return;
            case "unpin":
                updateDishFields(dishId, uw -> uw.set(Dish::getBoardSort, null));
                return;
            case "move_up_red":
                moveOnBoard(dish, true, -1);
                return;
            case "move_down_red":
                moveOnBoard(dish, true, 1);
                return;
            case "move_up_black":
                moveOnBoard(dish, false, -1);
                return;
            case "move_down_black":
                moveOnBoard(dish, false, 1);
                return;
            default:
                throw new BusinessException("无效的操作: " + action);
        }
        dishMapper.updateById(dish);
    }

    public void batchInterveneBoard(List<Long> dishIds, String action) {
        for (Long dishId : dishIds) {
            interveneBoard(dishId, action);
        }
    }

    public void triggerCalculate() {
        boardCalculationTask.calculateRedBlackBoard();
    }

    /** MyBatis-Plus updateById 会忽略 null 字段，需用 Wrapper 显式清空 */
    private void updateDishFields(Long dishId, Function<LambdaUpdateWrapper<Dish>, LambdaUpdateWrapper<Dish>> setter) {
        LambdaUpdateWrapper<Dish> wrapper = new LambdaUpdateWrapper<Dish>().eq(Dish::getDishId, dishId);
        dishMapper.update(null, setter.apply(wrapper));
    }

    private List<Dish> loadActiveDishes() {
        return dishMapper.selectList(new LambdaQueryWrapper<Dish>().eq(Dish::getStatus, 1));
    }

    private List<Map<String, Object>> buildBoardEntries(List<Dish> dishes, boolean redBoard, boolean includeHidden) {
        List<Map<String, Object>> entries = new ArrayList<>();
        for (Dish dish : dishes) {
            if (!includeHidden && isHidden(dish)) {
                continue;
            }
            BoardMatch match = resolveBoardMatch(dish, redBoard);
            if (!match.onBoard) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("dishId", dish.getDishId());
            item.put("name", dish.getName());
            item.put("avgScore", dish.getAvgScore());
            item.put("saleCount", dish.getSaleCount());
            item.put("reviewCount", dish.getReviewCount());
            item.put("boardStatus", dish.getBoardStatus());
            item.put("boardSort", dish.getBoardSort());
            item.put("boardHidden", dish.getBoardHidden() != null ? dish.getBoardHidden() : 0);
            item.put("source", match.manual ? "manual" : "auto");
            item.put("pinned", dish.getBoardSort() != null);
            entries.add(item);
        }
        sortBoardEntries(entries, redBoard);
        if (!includeHidden) {
            entries = entries.stream().limit(BOARD_LIMIT).collect(Collectors.toList());
        }
        return entries;
    }

    private BoardMatch resolveBoardMatch(Dish dish, boolean redBoard) {
        String status = dish.getBoardStatus();
        if (redBoard) {
            if ("red".equals(status)) {
                return new BoardMatch(true, true);
            }
            if ("red_remove".equals(status) || "black".equals(status)) {
                return new BoardMatch(false, false);
            }
            if (qualifiesAutoRed(dish)) {
                return new BoardMatch(true, false);
            }
            return new BoardMatch(false, false);
        }
        if ("black".equals(status)) {
            return new BoardMatch(true, true);
        }
        if ("black_remove".equals(status) || "red".equals(status)) {
            return new BoardMatch(false, false);
        }
        if (qualifiesAutoBlack(dish)) {
            return new BoardMatch(true, false);
        }
        return new BoardMatch(false, false);
    }

    public boolean qualifiesAutoRed(Dish dish) {
        return dish.getAvgScore() != null && dish.getAvgScore().compareTo(RED_SCORE_MIN) >= 0
                && dish.getSaleCount() != null && dish.getSaleCount() > 100;
    }

    public boolean qualifiesAutoBlack(Dish dish) {
        return dish.getAvgScore() != null && dish.getAvgScore().compareTo(BLACK_SCORE_MAX) < 0
                && dish.getReviewCount() != null && dish.getReviewCount() >= 5;
    }

    private void sortBoardEntries(List<Map<String, Object>> entries, boolean redBoard) {
        entries.sort((a, b) -> {
            Integer sortA = (Integer) a.get("boardSort");
            Integer sortB = (Integer) b.get("boardSort");
            boolean pinnedA = sortA != null;
            boolean pinnedB = sortB != null;
            if (pinnedA != pinnedB) {
                return pinnedA ? -1 : 1;
            }
            if (pinnedA && pinnedB) {
                int cmp = Integer.compare(sortA, sortB);
                if (cmp != 0) {
                    return cmp;
                }
            }
            BigDecimal scoreA = (BigDecimal) a.get("avgScore");
            BigDecimal scoreB = (BigDecimal) b.get("avgScore");
            if (scoreA == null && scoreB == null) {
                return 0;
            }
            if (scoreA == null) {
                return 1;
            }
            if (scoreB == null) {
                return -1;
            }
            return redBoard ? scoreB.compareTo(scoreA) : scoreA.compareTo(scoreB);
        });
    }

    private void pinOnBoard(Dish dish, boolean redBoard) {
        BoardMatch match = resolveBoardMatch(dish, redBoard);
        if (!match.onBoard) {
            throw new BusinessException(redBoard ? "该菜品不在红榜中，请先加入红榜" : "该菜品不在黑榜中，请先加入黑榜");
        }
        List<Dish> dishes = loadActiveDishes();
        int minSort = dishes.stream()
                .filter(d -> resolveBoardMatch(d, redBoard).onBoard && d.getBoardSort() != null)
                .mapToInt(Dish::getBoardSort)
                .min()
                .orElse(1);
        dish.setBoardSort(minSort - 1);
        dishMapper.updateById(dish);
    }

    private void moveOnBoard(Dish dish, boolean redBoard, int direction) {
        List<Dish> dishes = loadActiveDishes();
        List<Map<String, Object>> entries = buildBoardEntries(dishes, redBoard, true);
        int index = -1;
        for (int i = 0; i < entries.size(); i++) {
            if (dish.getDishId().equals(entries.get(i).get("dishId"))) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new BusinessException("该菜品不在当前榜单中");
        }
        int target = index + direction;
        if (target < 0 || target >= entries.size()) {
            return;
        }
        Long otherId = (Long) entries.get(target).get("dishId");
        Dish other = dishMapper.selectById(otherId);
        if (other == null) {
            return;
        }
        int sortA = dish.getBoardSort() != null ? dish.getBoardSort() : index + 100;
        int sortB = other.getBoardSort() != null ? other.getBoardSort() : target + 100;
        dishMapper.update(null, new LambdaUpdateWrapper<Dish>()
                .eq(Dish::getDishId, dish.getDishId())
                .set(Dish::getBoardSort, sortB));
        dishMapper.update(null, new LambdaUpdateWrapper<Dish>()
                .eq(Dish::getDishId, other.getDishId())
                .set(Dish::getBoardSort, sortA));
    }

    private boolean isHidden(Dish dish) {
        return dish.getBoardHidden() != null && dish.getBoardHidden() == 1;
    }

    private DishVO mapToDishVO(Map<String, Object> item) {
        DishVO vo = new DishVO();
        vo.setDishId((Long) item.get("dishId"));
        vo.setName((String) item.get("name"));
        vo.setAvgScore((BigDecimal) item.get("avgScore"));
        vo.setSaleCount((Integer) item.get("saleCount"));
        vo.setReviewCount((Integer) item.get("reviewCount"));
        return vo;
    }

    private static class BoardMatch {
        final boolean onBoard;
        final boolean manual;

        BoardMatch(boolean onBoard, boolean manual) {
            this.onBoard = onBoard;
            this.manual = manual;
        }
    }
}
