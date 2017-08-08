package wbs.api.resource;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.api.module.ApiSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("variable")
@PrototypeComponent ("apiVariableSpec")
public
class ApiVariableSpec
	implements ApiSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataChildren (
		direct = true)
	List <Object> builders;

}
