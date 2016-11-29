package client.entity;

import client.util.EDIMessageHeaders;
import client.util.CryptographyService;

import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * Created by a1 on 13.11.16.
 */
public class EncryptedEDIMessage {

    private EDIMessage originalMessage;

    private byte[] encrypted;

    private EncryptionInfoMessage infoMessage;

    private EncryptedEDIMessage(EDIMessage ediMessage, String password, int infoMessageHeader) {
        this.originalMessage = ediMessage;
        this.encrypted = CryptographyService.RC4(ediMessage.toString(), password);
        this.infoMessage = new EncryptionInfoMessage(infoMessageHeader);
    }

    public static EncryptedEDIMessage create(EDIMessage ediMessage, String password, int infoMessageHeader) {
        return new EncryptedEDIMessage(ediMessage, password, infoMessageHeader);
    }

    /** It is the same {@link EDIMessage#toString()} but encrypted with RC4 */
    @Override
    public String toString() {
        return originalMessage.toString();
    }

    public byte[] getEncrypted() {
        return encrypted;
    }

    public EncryptionInfoMessage getInfoMessage() {
        return infoMessage;
    }

    public class EncryptionInfoMessage {

        private int header;

        private static final String algorithm = "RC4";

        private static final int NUMBER_OF_PACKAGES = 8;

        private SecretKey RC4Key;

        EncryptionInfoMessage(int infoMessageHeader) {
            this.RC4Key = CryptographyService.lastRC4Key;
            this.header = infoMessageHeader;
        }

        @Override
        public String toString() {
            return EDIMessageHeaders.PACKAGE_HEADER.getName() + EDIMessageHeaders.DIVIDER.getName() + String.format("%04d", header) + EDIMessageHeaders.SEGMENT_END.getName() +
                    EDIMessageHeaders.MESSAGE_START.getName() + EDIMessageHeaders.SEGMENT_END.getName() +
                    EDIMessageHeaders.DATE_TIME_PERIOD.getName() + EDIMessageHeaders.DIVIDER.getName() + originalMessage.getMessageDate().getYear() + originalMessage.getMessageDate().getMonthValue() + originalMessage.getMessageDate().getDayOfMonth() + ":" + originalMessage.getMessageDate().getHour() + originalMessage.getMessageDate().getMinute() + ":" + originalMessage.getMessageDuration().toHours() + EDIMessageHeaders.SEGMENT_END.getName() +
                    EDIMessageHeaders.DESCRIPTION.getName() + EDIMessageHeaders.DIVIDER.getName() + String.format("%04d", originalMessage.getHeader()) + EDIMessageHeaders.SEGMENT_END.getName() +
                    EDIMessageHeaders.ENCRYPTION_ALGORITHM.getName() + EDIMessageHeaders.DIVIDER.getName() + algorithm + EDIMessageHeaders.SEGMENT_END.getName() +
                    EDIMessageHeaders.ENCRYPTION_KEY.getName() + EDIMessageHeaders.DIVIDER.getName() + Base64.getEncoder().encodeToString(RC4Key.getEncoded()) + EDIMessageHeaders.SEGMENT_END.getName() +
                    EDIMessageHeaders.MESSAGE_END.getName() + EDIMessageHeaders.SEGMENT_END.getName() +
                    EDIMessageHeaders.PACKAGE_END.getName() + EDIMessageHeaders.DIVIDER.getName() + String.format("%04d", NUMBER_OF_PACKAGES) + EDIMessageHeaders.SEGMENT_END.getName();
        }

    }

}
