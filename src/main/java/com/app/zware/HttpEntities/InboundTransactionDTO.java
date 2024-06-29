package com.app.zware.HttpEntities;

import com.app.zware.Entities.InboundTransactionDetail;
import com.app.zware.Entities.Item;
import lombok.Data;

import java.time.LocalDate;
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

    private Integer warehouse_id;

    private List<InboundDetailsDTO> details;



}
