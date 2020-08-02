package empty.folder.instagram.Utils;

import android.os.Environment;

public class FilePaths {

    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String CAMERA_DIR = ROOT_DIR + "/DCIM/Camera";
    public String PICTURES_DIR = ROOT_DIR + "/Pictures";

    public String FIREBASE_IMAGE_STORAGE = "photos/users/";
}
