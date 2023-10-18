package com.example.solumonbackend.global.recommend;

import com.example.solumonbackend.post.entity.Recommend;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.data.RepositoryItemWriter;

@AllArgsConstructor
public class RepositoryItemListWriter<R> extends RepositoryItemWriter<List<Recommend>> {

  private RepositoryItemWriter<Recommend> repositoryItemWriter;

  @Override
  public void write(List<? extends List<Recommend>> items) throws Exception {
    List<Recommend> totalList = new ArrayList<>();
    for (List<Recommend> list : items) {
      totalList.addAll(list);
    }
    repositoryItemWriter.write(totalList);
  }
}
