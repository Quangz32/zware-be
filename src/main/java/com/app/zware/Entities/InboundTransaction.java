package com.app.zware.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Date;
import lombok.Data;

@Entity(name = "inboundtransactions")
@Data
public class InboundTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Date date;

  private Integer maker_id;

  private String status;

  private Integer source; //warehouse id

  private String external_source;

  private boolean isdeleted = false;

  private Integer warehouse_id;
}
