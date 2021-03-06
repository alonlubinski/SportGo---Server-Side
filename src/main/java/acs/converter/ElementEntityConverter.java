package acs.converter;

import org.springframework.stereotype.Component;

import acs.boundary.ElementBoundary;
import acs.boundary.boundaryUtils.CreatedBy;
import acs.boundary.boundaryUtils.ElementId;
import acs.boundary.boundaryUtils.Location;
import acs.boundary.boundaryUtils.UserId;
import acs.data.ElementEntity;
import acs.data.LocationEntity;

@Component
public class ElementEntityConverter {
	public ElementBoundary fromEntity(ElementEntity elementEntity) {
		ElementId elementId = null;
		if (elementEntity.getElementId() != null) {
			String[] elementIdParts = elementEntity.getElementId().split("@@");
			if (elementIdParts.length >= 1) {
				elementId = new ElementId();
				elementId.setDomain(elementIdParts[0]);
				if(elementIdParts.length >= 2) {
					elementId.setId(elementIdParts[1]);
				}
			}
		}

		CreatedBy createdBy;
		if (elementEntity.getCreatedBy() != null) {
			String[] createdByParts = elementEntity.getCreatedBy().split("@@");
			createdBy = new CreatedBy();
			createdBy.setUserId(new UserId(createdByParts[0], createdByParts[1]));
		} else {
			createdBy = null;
		}

		Location location;
		if (elementEntity.getLocation() != null) {

			double lat = elementEntity.getLocation().getLat();
			double lng = elementEntity.getLocation().getLng();
			location = new Location(lat, lng);

		} else {
			location = null;
		}
		return new ElementBoundary(elementId, elementEntity.getType(), elementEntity.getActive(),
				elementEntity.getName(), elementEntity.getCreatedTimestamp(), createdBy, location,
				elementEntity.getElementAttributes());
	}

	public ElementEntity toEntity(ElementBoundary elementBoundary) {
		ElementEntity elementEntity = new ElementEntity();
		if (elementBoundary.getElementId() != null) {
			elementEntity.setElementId(
					elementBoundary.getElementId().getDomain() + "@@" + elementBoundary.getElementId().getId());
		} else {
			elementEntity.setElementId(null);
		}

		elementEntity.setType(elementBoundary.getType());

		elementEntity.setName(elementBoundary.getName());

		if (elementBoundary.getActive() != null) {
			elementEntity.setActive(elementBoundary.getActive());
		} else {
			elementEntity.setActive(false);
		}
		elementEntity.setCreatedTimestamp(elementBoundary.getCreatedTimestamp());

		if (elementBoundary.getCreatedBy() != null) {
			elementEntity.setCreatedBy(elementBoundary.getCreatedBy().getUserId().getDomain() + "@@"
					+ elementBoundary.getCreatedBy().getUserId().getEmail());
		} else {
			elementEntity.setCreatedBy(null);
		}

		if (elementBoundary.getLocation() != null) {
			LocationEntity locationEntity = new LocationEntity();
			locationEntity.setLat(elementBoundary.getLocation().getLat());
			locationEntity.setLng(elementBoundary.getLocation().getLng());
			elementEntity.setLocation(locationEntity);
					
		} else {
			elementEntity.setLocation(null);
		}

		elementEntity.setElementAttributes(elementBoundary.getElementAttributes());

		return elementEntity;
	}

}
