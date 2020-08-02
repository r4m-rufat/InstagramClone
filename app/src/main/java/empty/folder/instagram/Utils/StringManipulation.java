package empty.folder.instagram.Utils;

public class StringManipulation {

    public static String expandUsername(String username){
        return username.replace(".", " ");
    }

    public static String codenseUsername(String username){
        return username.replace(" ", ".");
    }


    public static String getTags(String string) {
        if (string.indexOf("#") > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            char[] chars = string.toCharArray();
            boolean foundChar = false;
            for (char c : chars) {
                if (c == '#') {
                    foundChar = true;
                    stringBuilder.append(c);
                } else {
                    if (foundChar) {
                        stringBuilder.append(c);
                    }
                }
                if (c == ' ') {

                    foundChar = false;

                }
            }

            String stringword = stringBuilder.toString().replace(" ", "").replace("#", ",#");
            return stringword.substring(1, stringword.length());
        }

        return string;
    }
}
