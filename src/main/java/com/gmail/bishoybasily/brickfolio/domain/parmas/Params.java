package com.gmail.bishoybasily.brickfolio.domain.parmas;

import static org.springframework.util.ObjectUtils.isEmpty;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;

import com.gmail.bishoybasily.brickfolio.utils.StreamUtils;

@Getter
@Setter
@ToString
@SuperBuilder
@Accessors(chain = true)
@NoArgsConstructor
public class Params<T> {

  private Integer page;
  private Integer count;

  @Singular
  private List<String> ids;

  @Singular
  private List<String> sortFields;

  @Singular
  private List<Direction> sortDirections;

  public boolean isPaginationPresented() {
    return !isEmpty(page) && !isEmpty(count);
  }

  public boolean isSortPresented() {
    return !isEmpty(sortFields) && !isEmpty(sortDirections);
  }

  public Pageable pageable() {
    if (isSortPresented())
      return PageRequest.of(page, count, sort());
    return PageRequest.of(page, count);
  }

  public Sort sort() {
    final var propertiesStream = sortFields.stream();
    final var directionsStream = sortDirections.stream();
    final var orders = StreamUtils.zip(directionsStream, propertiesStream, Order::new).toList();
    return Sort.by(orders);
  }

  public Specification<T> toSpecification() {
    Specification<T> spec = (_, query, criteriaBuilder) -> {
      query.distinct(true);
      return criteriaBuilder.conjunction();
    };

    if (!isEmpty(ids))
      spec = spec.and((root, _, _) -> {
        return root.get("id").in(ids);
      });

    return spec;
  }
}
