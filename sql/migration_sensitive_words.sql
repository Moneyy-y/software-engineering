-- 补充营销引流类敏感词（已有库执行一次即可）
USE catering;

INSERT IGNORE INTO `sensitive_word` (`content`, `category`, `status`) VALUES
('微信', 'spam', 1),
('加微信', 'spam', 1),
('优惠券', 'spam', 1),
('扫码', 'spam', 1),
('兼职', 'spam', 1);
