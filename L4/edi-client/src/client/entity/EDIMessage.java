package client.entity;

import client.util.EDIMessageHeaders;
import client.util.EDIMessageHeaders.DocumentType;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Currency;

/**
 * Created by a1 on 10.11.16.
 *
 * Describes EDI message to be sent.
 * ( Not encrypted )
 */
public class EDIMessage {

    private int header;

    private LocalDateTime messageDate;

    private Duration messageDuration;

    private DocumentType documentType;

    private String sendersName;

    private String sendersBankName;

    private String sendersBankCode;

    private String sendersBankAccount;

    private String recipientsName;

    private String recipientsBankName;

    private String recipientsBankCode;

    private String recipientsBankAccount;

    private String transactionPurpose;

    private Currency currency;

    private BigDecimal summary;

    public static final int NUMBER_OF_PACKAGES = 17;

    private EDIMessage(int header, LocalDateTime messageDate, Duration messageDuration, DocumentType documentType,
                       String sendersName, String sendersBankName, String sendersBankCode, String sendersBankAccount,
                       String recipientsName, String recipientsBankName, String recipientsBankCode,
                       String recipientsBankAccount, String transactionPurpose, Currency currency, BigDecimal summary) {

        this.header = header;
        this.messageDate = messageDate;
        this.messageDuration = messageDuration;
        this.documentType = documentType;
        this.sendersName = sendersName;
        this.sendersBankName = sendersBankName;
        this.sendersBankCode = sendersBankCode;
        this.sendersBankAccount = sendersBankAccount;
        this.recipientsName = recipientsName;
        this.recipientsBankName = recipientsBankName;
        this.recipientsBankCode = recipientsBankCode;
        this.recipientsBankAccount = recipientsBankAccount;
        this.transactionPurpose = transactionPurpose;
        this.currency = currency;
        this.summary = summary;
    }

    public static EDIMessage create(int header, LocalDateTime messageDate, Duration messageDuration, DocumentType documentType,
                             String sendersName, String sendersBankName, String sendersBankCode, String sendersBankAccount,
                             String recipientsName, String recipientsBankName, String recipientsBankCode,
                             String recipientsBankAccount, String transactionPurpose, Currency currency, BigDecimal summary) {

        return new EDIMessage(
                header, messageDate, messageDuration, documentType, sendersName, sendersBankName,
                sendersBankCode, sendersBankAccount, recipientsName, recipientsBankName, recipientsBankCode,
                recipientsBankAccount, transactionPurpose, currency, summary);
    }

    @Override
    public String toString() {
        // String.format("%04d", header) sets output of number for example 16 as 0016
        return EDIMessageHeaders.PACKAGE_HEADER.getName() + EDIMessageHeaders.DIVIDER.getName() + String.format("%04d", header) + EDIMessageHeaders.SEGMENT_END.getName() +
                EDIMessageHeaders.MESSAGE_START.getName() + EDIMessageHeaders.SEGMENT_END.getName() +
                EDIMessageHeaders.DATE_TIME_PERIOD.getName() + EDIMessageHeaders.DIVIDER.getName() + messageDate.getYear() + messageDate.getMonthValue() + messageDate.getDayOfMonth() + ":" + messageDate.getHour() + messageDate.getMinute() + ":" + messageDuration.toHours() + EDIMessageHeaders.SEGMENT_END.getName() +
                EDIMessageHeaders.DOCUMENT_NAME.getName() + EDIMessageHeaders.DIVIDER.getName() + documentType.getType() + EDIMessageHeaders.SEGMENT_END.getName() +
                EDIMessageHeaders.SENDER.getName() + EDIMessageHeaders.DIVIDER.getName() + sendersName + EDIMessageHeaders.SEGMENT_END.getName() +
                EDIMessageHeaders.SENDERS_BANK_NAME.getName() + EDIMessageHeaders.DIVIDER.getName() + sendersBankName + EDIMessageHeaders.SEGMENT_END.getName() +
                EDIMessageHeaders.SENDERS_BANK_CODE.getName() + EDIMessageHeaders.DIVIDER.getName() + sendersBankCode + EDIMessageHeaders.SEGMENT_END.getName() +
                EDIMessageHeaders.SENDERS_BANK_ACCOUNT.getName() + EDIMessageHeaders.DIVIDER.getName() + sendersBankAccount + EDIMessageHeaders.SEGMENT_END.getName() +
                EDIMessageHeaders.RECIPIENT.getName() + EDIMessageHeaders.DIVIDER.getName() + recipientsName + EDIMessageHeaders.SEGMENT_END.getName() +
                EDIMessageHeaders.RECIPIENTS_BANK_NAME.getName() + EDIMessageHeaders.DIVIDER.getName() + recipientsBankName + EDIMessageHeaders.SEGMENT_END.getName() +
                EDIMessageHeaders.RECIPIENTS_BANK_CODE.getName() + EDIMessageHeaders.DIVIDER.getName() + recipientsBankCode + EDIMessageHeaders.SEGMENT_END.getName() +
                EDIMessageHeaders.RECIPIENTS_BANK_ACCOUNT.getName() + EDIMessageHeaders.DIVIDER.getName() + recipientsBankAccount + EDIMessageHeaders.SEGMENT_END.getName() +
                EDIMessageHeaders.DESCRIPTION.getName() + EDIMessageHeaders.DIVIDER.getName() + transactionPurpose + EDIMessageHeaders.SEGMENT_END.getName() +
                EDIMessageHeaders.CURRENCY.getName() + EDIMessageHeaders.DIVIDER.getName() + currency.getCurrencyCode() + EDIMessageHeaders.SEGMENT_END.getName() +
                EDIMessageHeaders.SUMMARY.getName() + EDIMessageHeaders.DIVIDER.getName() + summary + EDIMessageHeaders.SEGMENT_END.getName() +
                EDIMessageHeaders.MESSAGE_END.getName()+ EDIMessageHeaders.SEGMENT_END.getName() +
                EDIMessageHeaders.PACKAGE_END.getName() + EDIMessageHeaders.DIVIDER.getName() + String.format("%04d", NUMBER_OF_PACKAGES) + EDIMessageHeaders.SEGMENT_END.getName();
    }

    public int getHeader() {
        return header;
    }

    public LocalDateTime getMessageDate() {
        return messageDate;
    }

    public Duration getMessageDuration() {
        return messageDuration;
    }
}
