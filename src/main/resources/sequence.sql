CREATE TABLE IF NOT EXISTS `sequence` (
    added_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    stub VARCHAR(64) NOT NULL, # sequence name
    offset INT NOT NULL, # the next sequence starts at, defaults to 1
    bucket SMALLINT NOT NULL, # the size of pre-allocated sequence ids in memory for faster access, defaults to 100
    step TINYINT NOT NULL, # the delta between each id
    version INT NOT NULL, # just keep increasing, how many batches processed
    UNIQUE KEY `stub` (`stub`)
) ENGINE = InnoDB CHARACTER SET = ascii COLLATE = ascii_bin;