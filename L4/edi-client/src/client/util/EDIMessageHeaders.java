package client.util;

/**
 * Created by a1 on 12.11.16.
 */
public enum EDIMessageHeaders {

    PACKAGE_HEADER("PKH"),                  // Заголовок пакета
    PACKAGE_END("PKE"),                     // Кінець пакета
    MESSAGE_START("MST"),                   // Початок повідомлення
    MESSAGE_END("MED"),                     // Кінець повідомлення
    NAME("NME"),                            // Назва
    DATE_TIME_PERIOD("DTP"),                // Дата/Час/Період
    SENDER("SDR"),                          // Покупець
    DESCRIPTION("DPN"),                     // Опис пункту
    PAYER("PER"),                           // Платник
    CURRENCY("CRY"),                        // Валюта
    RECIPIENT("RPT"),                       // Отримувач
    ORDER_PRICE("OPR"),                     // Ціна замовлення
    GOODS_QUANTITY("GQT"),                  // Кількість товару
    DIVIDER("+"),                           // Розділовий символ
    SEGMENT_END("'"),                       // Знак закінчення сегмента
    DOCUMENT_NAME("DNM"),                   // Назва документа
    GOOD("GOD"),                            // Товар
    MEASURE_UNIT("MUT"),                    // Одиниця виміру
    SUMMARY("SUM"),                         // Сума
    SENDERS_BANK_NAME("SBN"),               // Банк відправника
    SENDERS_BANK_ACCOUNT("SBA"),            // Рахунок відправника
    SENDERS_BANK_CODE("SBC"),               // МФО банку відправника
    RECIPIENTS_BANK_NAME("RBN"),            // Банк відправника
    RECIPIENTS_BANK_ACCOUNT("RBA"),         // Рахунок одержувача
    RECIPIENTS_BANK_CODE("RBC"),            // МФО банку одержувача
    INITIALIZATION_VECTOR("IVR"),           // Вектор ініціалізації
    ENCRYPTION_ALGORITHM("EAM"),            // Алгоритм шифрування
    ENCRYPTION_KEY("EKY"),                  // Ключ шифрування
    DIGITAL_SIGNATURE_ALGORITHM("DSA"),     // Алгоримт цифрового підпису
    DIGITAL_SIGNATURE_KEY("DSK");           // Ключ для перевірки цифрового підпису


    private String name;

    EDIMessageHeaders(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public enum DocumentType {

        PAYMENT("PMT", "Payment"),      // Оплата
        INVOICE("IVE", "Invoice");      // Накладна

        private String type;

        private String fullName;

        DocumentType(String type, String fullName) {
            this.type = type;
            this.fullName = fullName;
        }

        public String getType() {
            return type;
        }

        public String getFullName() {
            return fullName;
        }
    }
}
