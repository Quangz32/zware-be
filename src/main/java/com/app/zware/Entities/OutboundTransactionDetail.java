package com.app.zware.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity(name = "outboundtransactiondetails")
@Data
public class OutboundTransactionDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private Integer transaction_id;
  private Integer item_id;
  private Integer quantity;
  private Integer zone_id;
}
