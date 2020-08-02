package empty.folder.instagram.Utils;

import java.io.File;
import java.util.ArrayList;

public class FileSearch {

    /**
     * Search directories in sd card and return directories path
     * @param directory
     * @return
     */
    public static ArrayList<String> getDirectoryPath(String directory){

        ArrayList<String> pathArray = new ArrayList<>();

        File file = new File(directory);
        File[] listFiles = file.listFiles();

        for (int i = 0;i<listFiles.length;i++){
            if (listFiles[i].isDirectory()){
                pathArray.add(listFiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }

    /**
     * Search a files in sd card and return files path
     * @param directory
     * @return
     */
    public static ArrayList<String> getFilePath(String directory){

        ArrayList<String> pathArray = new ArrayList<>();

        File file = new File(directory);
        File[] listFiles = file.listFiles();

        for (int i = 0;i<listFiles.length;i++){
            if (listFiles[i].isFile()){
                pathArray.add(listFiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }

}
