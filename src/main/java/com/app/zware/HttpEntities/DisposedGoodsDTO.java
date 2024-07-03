package com.app.zware.HttpEntities;

import java.time.LocalDate;
import java.util.Objects;
import lombok.Data;

@Data
public class DisposedGoodsDTO {
    private Integer zone_id;
    private Integer product_id;
    private LocalDate expire_date;
    private Integer quantity;
    private String reason;

    //For check duplicate
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DisposedGoodsDTO detail = (DisposedGoodsDTO) obj;
        return product_id.equals(detail.product_id) &&
            zone_id.equals(detail.zone_id) &&
            expire_date.equals(detail.expire_date);
    }

    //For check duplicate
    @Override
    public int hashCode() {
        return Objects.hash(product_id, zone_id, expire_date);
    }
}
