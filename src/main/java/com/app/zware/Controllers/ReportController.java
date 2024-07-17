package com.app.zware.Controllers;

import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.HttpEntities.WarehouseInventoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    // customerResponse
    CustomResponse customResponse = new CustomResponse();

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReportController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("")
    public ResponseEntity<?> getWarehouseInventoryReport() {
        String sql = "SELECT p.name AS product_name, " +
                "p.supplier AS supplier, " +
                "p.measure_unit AS measure_unit, " +
                "SUM(wi.quantity) AS total_quantity " +
                "FROM WarehouseItems wi JOIN Items i ON wi.item_id = i.id " +
                "JOIN Products p ON i.product_id = p.id " +
                "GROUP BY p.name, p.supplier, p.measure_unit " +
                "ORDER BY p.name";

        List<WarehouseInventoryDTO> inventoryList = jdbcTemplate.query(sql, (resultSet, i) -> {
            WarehouseInventoryDTO inventoryDTO = new WarehouseInventoryDTO();
            inventoryDTO.setProductName(resultSet.getString("product_name"));
            inventoryDTO.setSupplier(resultSet.getString("supplier"));
            inventoryDTO.setMeasureUnit(resultSet.getString("measure_unit"));
            inventoryDTO.setTotalQuantity(resultSet.getLong("total_quantity"));
            return inventoryDTO;
        });

        customResponse.setAll(true,"Get List Inventory success",inventoryList);
        return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
}
