package com.gmail.bishoybasily.brickfolio.domain.parmas;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import com.gmail.bishoybasily.brickfolio.domain.entity.Developer;

@Getter
@Setter
@ToString
@SuperBuilder
@Accessors(chain = true)
@NoArgsConstructor
public class DeveloperParams extends Params<Developer> {

}
