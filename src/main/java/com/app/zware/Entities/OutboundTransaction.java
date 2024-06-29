package com.app.zware.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.Data;

@Entity(name = "outboundtransactions")
@Data
public class OutboundTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private LocalDate date;
  private Integer maker_id;
  private String status;
  private Integer destination;
  private String external_destination;
  private Boolean isdeleted = false;
  private Integer warehouse_id;
}
