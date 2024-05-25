package com.app.zware.Service;

import com.app.zware.Entities.Item;
import com.app.zware.Repositories.ItemRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

  @Autowired
  ItemRepository itemRepository;

  public List<Item> getAllItems() {
    return itemRepository.findAll();
  }

  public Item createItem(Item request) {
    return itemRepository.save(request);
  }

  public Item getItemById(int id) {
    return itemRepository.findById(id).orElseThrow(() -> new RuntimeException("Not Found Item"));
  }

  public boolean checkIdItemExist(int id) {
    return itemRepository.existsById(id);
  }

  public void deleteItemById(int id) {
    itemRepository.deleteById(id);
  }

  public Item updateItem(int id, Item request) {
    Item item = getItemById(id);
    Optional.ofNullable(request.getProduct_id()).ifPresent(item::setProduct_id);
    Optional.ofNullable(request.getExpire_date()).ifPresent(item::setExpire_date);
    return itemRepository.save(item);


  }


}
