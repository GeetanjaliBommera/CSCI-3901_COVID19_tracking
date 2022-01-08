use covid;

CREATE TABLE mobilehash (
    mhash VARCHAR(200) PRIMARY KEY NOT NULL
);
CREATE TABLE contacts (
    userid VARCHAR(200) NOT NULL,
    neighbourid VARCHAR(200) NOT NULL,
    date INT,
    duration INT,
    FOREIGN KEY (userid)
        REFERENCES mobilehash (mhash),
    FOREIGN KEY (neighbourid)
        REFERENCES mobilehash (mhash)
);

CREATE TABLE testresults (
    testhash VARCHAR(200) PRIMARY KEY NOT NULL,
    date INT NOT NULL,
    result VARCHAR(200) NOT NULL,
    mhash VARCHAR(200),
    FOREIGN KEY (mhash)
        REFERENCES mobilehash (mhash)
);
CREATE TABLE shown (
    mhash VARCHAR(200) NOT NULL,
    testhash VARCHAR(200) NOT NULL,
    FOREIGN KEY (testhash)
        REFERENCES testresults (testhash),
    FOREIGN KEY (mhash)
        REFERENCES mobilehash (mhash),
    PRIMARY KEY (mhash , testhash)
);
