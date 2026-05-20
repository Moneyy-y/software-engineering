-- 举报示例数据（已有库可单独执行）
INSERT INTO `report` (`user_id`, `target_type`, `target_id`, `reason`, `description`, `status`, `handler_id`, `handle_result`, `handle_time`)
SELECT 2, 'post', 2, '虚假信息', '内容与事实不符，存在误导', 'pending', NULL, NULL, NULL FROM DUAL
WHERE EXISTS (SELECT 1 FROM post WHERE post_id = 2)
  AND NOT EXISTS (SELECT 1 FROM report WHERE target_type = 'post' AND target_id = 2 AND status = 'pending' AND reason = '虚假信息');

INSERT INTO `report` (`user_id`, `target_type`, `target_id`, `reason`, `description`, `status`, `handler_id`, `handle_result`, `handle_time`)
SELECT 3, 'review', 1, '辱骂谩骂', '评价含不当人身攻击用语', 'pending', NULL, NULL, NULL FROM DUAL
WHERE EXISTS (SELECT 1 FROM review WHERE review_id = 1)
  AND NOT EXISTS (SELECT 1 FROM report WHERE target_type = 'review' AND target_id = 1 AND status = 'pending');
