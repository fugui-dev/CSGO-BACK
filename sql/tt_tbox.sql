ALTER TABLE `tt_box`
    ADD COLUMN `high_value_open_num` bigint DEFAULT '0' AFTER `box_kind`,
ADD COLUMN `must_high_value_open_num` int DEFAULT '0' AFTER `high_value_open_num`;