package model;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Location {
	private int id;
	private String client_id;
	private Double latitude;
	private Double longitude;
	private Long time;
	private String district;
}
