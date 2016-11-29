package server.util;

/**
 * Created by a1 on 26.11.16.
 */
public class EDIValidator {

    /**
     * In the end of any EDI message I am expecting something like PKE+0008.
     * So, this method parsing last value and real number of blocks, split by symbol '.
     */
    public static boolean validate(String message) {
        try {
            String[] blocks = message.split("'");
            Integer neededNumberOfBlocks = Integer.parseInt(blocks[blocks.length - 1].split("[+]")[1]);
            return neededNumberOfBlocks == blocks.length;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
