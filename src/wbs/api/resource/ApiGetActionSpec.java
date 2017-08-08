package wbs.api.resource;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.api.module.ApiSpec;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("get-action")
@PrototypeComponent ("apiGetActionSpec")
public
class ApiGetActionSpec
	implements ApiSpec {

	@DataAttribute (
		required = true)
	String name;

}
