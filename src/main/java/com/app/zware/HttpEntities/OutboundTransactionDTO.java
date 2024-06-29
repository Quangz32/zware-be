package com.app.zware.HttpEntities;

import com.app.zware.Entities.OutboundTransactionDetail;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class OutboundTransactionDTO {
    private Integer id;

    private LocalDate date;

    private Integer maker_id;

    private String status;

    private Integer destination; //warehouse_id

    private String external_destination;

    private boolean isdeleted = false;

    private Integer warehouse_id;


    private List<TransactionItem> transactionItems; // List of transaction items

    @Data
    public static class TransactionItem {
        private Integer productId;
        private Integer quantityProduct;
    }
}
