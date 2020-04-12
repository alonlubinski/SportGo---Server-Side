package acs.logic.mockup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import acs.boundary.ActionBoundary;
import acs.converter.ActionEntityConverter;
import acs.data.ActionEntity;
import acs.logic.ActionService;

@Service
public class ActionServiceMockup implements ActionService {
	
	private String projectName;
	private Map<String, ActionEntity> actionDatabase;
	private ActionEntityConverter actionEntityConverter;
	
	@Autowired
	public ActionServiceMockup(ActionEntityConverter actionEntityConverter) {
		this.actionEntityConverter = actionEntityConverter;
	}
	
	@Value("${spring.application.name:2020b.daniel.zusev}")
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	@PostConstruct
	public void init() {
		this.actionDatabase = Collections.synchronizedMap(new TreeMap<>());
	}

	@Override
	public Object invokeAction(ActionBoundary action) { //action have actionId = null
		
		ActionEntity actionInvoked = actionEntityConverter.toEntity(action);
		actionInvoked.setCreatedTimeStamp(new Date());
		actionDatabase.put(actionInvoked.getActionId(), actionInvoked);
		return actionInvoked;
	}

	@Override
	public List<ActionBoundary> getAllActions(String adminDomain, String adminEmail) {
		List<ActionBoundary> allActions = new ArrayList<>();
		for (ActionEntity actionEntity : actionDatabase.values()) {
			allActions.add(actionEntityConverter.fromEntity(actionEntity));
		}
		return allActions;
	}

	@Override
	public void deleteAllActions(String adminDomain, String adminEmail) {
		this.actionDatabase.clear();
		
	}
}
