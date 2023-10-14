package com.example.solumonbackend.global.recommend;

import com.example.solumonbackend.post.entity.Recommend;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.database.JpaItemWriter;

@AllArgsConstructor
public class JpaItemListWriter<R> extends JpaItemWriter<List<Recommend>> {
  private JpaItemWriter<Recommend> jpaItemWriter;

  @Override
  public void write(List<? extends List<Recommend>> items) {
    List<Recommend> totalList = new ArrayList<>();
    for(List<Recommend> list: items) {
      totalList.addAll(list);
    }
    jpaItemWriter.write(totalList);
  }
}
