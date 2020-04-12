package acs.converter;

import acs.ActionId;
import acs.Element;
import acs.ElementID;
import acs.InvokedBy;
import acs.UserId;
import acs.boundary.ActionBoundary;
import acs.data.ActionEntity;

public class ActionEntityConverter {
	
	public ActionBoundary fromEntity(ActionEntity actionEntity) {
		ActionId actionId;
		if(actionEntity.getActionId() != null) {
			String[] actionIdParts = actionEntity.getActionId().split("@@");
			actionId = new ActionId(actionIdParts[0], actionIdParts[1]);
		}
		else
			actionId = null;
		
		Element element;
		if(actionEntity.getElement() != null) {
			String[] elementParts = actionEntity.getElement().split("@@");
			element = new Element(new ElementID(elementParts[0], elementParts[1]));
		}
		else
			element = null;
		
		InvokedBy invokeBy;
		if(actionEntity.getInvokedBy() != null) {
			String[] invokeParts = actionEntity.getInvokedBy().split("@@");
			invokeBy = new InvokedBy(new UserId(invokeParts[0],invokeParts[1]));
		}
		else
			invokeBy = null;
			
		return new ActionBoundary(
				actionId,
				actionEntity.getType(),
				element,
				actionEntity.getCreatedTimeStamp(),
				invokeBy,
				actionEntity.getActionAttributes());
	}

	
	public ActionEntity toEntity(ActionBoundary actionBoundary) {
		
		ActionEntity actionEntity = new ActionEntity();
		if(actionBoundary.getActionId() != null) {
			actionEntity.setActionId(actionBoundary.getActionId().getDomain() + "@@" + actionBoundary.getActionId().getId());
		}
		else
			actionEntity.setActionId("@@");
		
		actionEntity.setType(actionBoundary.getType());
		if(actionBoundary.getElement() != null) {
			actionEntity.setElement(actionBoundary.getElement().getElementId().getDomain() + "@@" + actionBoundary.getElement().getElementId().getId());
		}
		else
			actionEntity.setElement("@@");
		
		actionEntity.setCreatedTimeStamp(actionBoundary.getCreatedTimeStamp());
		if(actionBoundary.getInvokedBy() != null) {
			actionEntity.setInvokedBy(actionBoundary.getInvokedBy().getUserId().getDomain() + "@@" + actionBoundary.getInvokedBy().getUserId().getEmail());
		}
		else
			actionEntity.setInvokedBy("@@");
		
		actionEntity.setActionAttributes(actionBoundary.getActionAttributes());
		
		return actionEntity;
	}
}