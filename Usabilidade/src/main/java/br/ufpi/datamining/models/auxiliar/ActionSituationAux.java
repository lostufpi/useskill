package br.ufpi.datamining.models.auxiliar;

import java.util.ArrayList;
import java.util.List;

import br.ufpi.datamining.components.PageViewDataMining;
import br.ufpi.datamining.models.ActionSingleDataMining;
import br.ufpi.datamining.models.FieldSearchTupleDataMining;
import br.ufpi.datamining.models.TaskDataMining;
import br.ufpi.datamining.models.enums.ActionTypeDataMiningEnum;
import br.ufpi.datamining.models.enums.FieldTypeDataMiningEnum;
import br.ufpi.datamining.models.enums.MomentTypeActionDataMiningEnum;

public class ActionSituationAux {
	
	private String action;
	private String element;
	private String id;
	private String location;
	private String situation;
	private String type;
	
	public ActionSituationAux() {
		super();
	}
	
	public ActionSituationAux(String action, String element, String id,
			String location, String situation, String type) {
		super();
		this.action = action;
		this.element = element;
		this.id = id;
		this.location = location;
		this.situation = situation;
		this.type = type;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getElement() {
		return element;
	}
	public void setElement(String element) {
		this.element = element;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getSituation() {
		return situation;
	}
	public void setSituation(String situation) {
		this.situation = situation;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public ActionSingleDataMining toActionSingleDataMining(TaskDataMining task){
		ActionSingleDataMining action = new ActionSingleDataMining();
		
		//action.setId(this.getId());
		
		ActionTypeDataMiningEnum actionType = ActionTypeDataMiningEnum.getTypeActionByAction(this.action);
		action.setActionType(actionType);
		
		MomentTypeActionDataMiningEnum momentType = MomentTypeActionDataMiningEnum.getMomentTypeByDesc(this.type);
		action.setMomentType(momentType);
		
		action.setTask(task);
		action.setDescription("Ação definida durante avaliação...");
		
		//add XPath
		List<FieldSearchTupleDataMining> elements = new ArrayList<FieldSearchTupleDataMining>();
		FieldSearchTupleDataMining fieldXPath = new FieldSearchTupleDataMining();
		fieldXPath.setAction(action);
		fieldXPath.setField("sXPath");
		fieldXPath.setType(FieldTypeDataMiningEnum.STRING);
		fieldXPath.setValue(this.element);
		elements.add(fieldXPath);
		action.setElementFiedlSearch(elements);
		
		//add Location
		if (this.location != null && !this.location.equals("")) {
			String[] locations = this.location.split("-");
			if (locations.length > 0) {
				int idx = 0;
				List<FieldSearchTupleDataMining> urlsFields = new ArrayList<FieldSearchTupleDataMining>();
				
				//com url
				FieldSearchTupleDataMining fieldURL = new FieldSearchTupleDataMining();
				fieldURL.setAction(action);
				fieldURL.setField("sUrl");
				fieldURL.setType(FieldTypeDataMiningEnum.STRING);
				if (locations[idx].charAt(0) == ':') {
					fieldURL.setValue(PageViewDataMining.getUrlFormatted(locations[idx]));
					idx++;
				} else {
					fieldURL.setValue("");
				}
				urlsFields.add(fieldURL);
				
				
				FieldSearchTupleDataMining fieldJhm = new FieldSearchTupleDataMining();
				fieldJhm.setAction(action);
				fieldJhm.setField("sJhm");
				fieldJhm.setType(FieldTypeDataMiningEnum.STRING);
				fieldJhm.setValue(locations[idx]);
				urlsFields.add(fieldJhm);
				idx++;
				
				FieldSearchTupleDataMining fieldSection = new FieldSearchTupleDataMining();
				fieldSection.setAction(action);
				fieldSection.setField("sStepJhm");
				fieldSection.setType(FieldTypeDataMiningEnum.STRING);
				fieldSection.setValue(locations[idx]);
				urlsFields.add(fieldSection);
				
				action.setUrlFieldSearch(urlsFields);
			}
		}
		
		return action;
	}
	
}
