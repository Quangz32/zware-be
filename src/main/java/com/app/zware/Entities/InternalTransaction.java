package com.app.zware.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.Data;

@Data
@Entity(name="internaltransactions")
public class InternalTransaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private String type;
  private Integer source_warehouse; //warehouse_id
  private Integer destination_warehouse ; //warehouse_id
  private LocalDate date;
  private Integer maker_id; //user_id
  private String status; //pending, shipping, completed, canceled
}
