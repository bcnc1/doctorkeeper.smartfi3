package com.thinoo.drcamlink2;

public class Constants {
    public static final class Invoke{
        public static final String VIDEO_RECORD = "video.record";
        public static final String UPLOAD_FILE_PATH = "upload.file.path";
        public static final String UPLOAD_FILE_KIND = "upload.file.kind";
        public static final String UPLOAD_FILE_NAME = "upload.file.name";
        public static final String UPLOAD_FILE_TYPE = "upload.file.type";
        public static final String UPLOAD_MESSAGE_CALLBACK = "upload.message.callback";
    }

    public static final class Upload{
        public static final String FILE_UPLOAD_SUCCESS = "file.upload.success";
        public static final String FILE_UPLOAD_FAIL = "file.upload.fail";
        public static final String CHAIN_CREATE_SUCCESS = "chain.create.success";
        public static final String CHAIN_CREATE_FAIL = "chain.create.fail";
    }

    public static final class Storage{
        public static final String BASE_URL = "https://ssproxy.ucloudbiz.olleh.com/v1/AUTH_3de4a999-ff5c-416a-ad30-77fcd4f9383f";
    }

    public static final class Chain{
        public static final String BASE_URL = "http://211.43.13.203:3000";
        public static final String CREATE = "/api/v1/proof/create";
    }

    public static final class Notification{
        public static final String NOTIFICATION_TITLE = "File Upload";
        public static final int  NOTIFICATION_PICTURE_ID = 2010;
        public static final int  NOTIFICATION_VIDEO_ID = 1010;
    }

    public static final long DELAY_TIME_MILLIS = 3000;
    public static final boolean FILE_N_DB_DELETE = true;
}
