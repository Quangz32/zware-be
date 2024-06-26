package com.app.zware.HttpEntities;

import com.app.zware.Entities.InboundTransactionDetail;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class InboundTransactionDTO {

    private Date date;

    private Integer maker_id;

    private String status;

    private Integer source; //warehouse_id

    private String external_source;

    private boolean isdeleted = false;

    private List<InboundTransactionDetail> details;

}
