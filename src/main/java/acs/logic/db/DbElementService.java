package acs.logic.db;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import acs.boundary.ElementBoundary;
import acs.boundary.ElementIdBoundary;
import acs.boundary.boundaryUtils.GardenInfo;
import acs.boundary.boundaryUtils.InfoFacility;
import acs.converter.AttributeConverter;
import acs.converter.ElementEntityConverter;
import acs.dal.ElementDao;
import acs.dal.UserDao;
import acs.data.ElementEntity;
import acs.data.FacilityType;
import acs.data.LocationEntity;
import acs.data.UserRole;
import acs.logic.ExtendedElementService;
import acs.logic.NotFoundException;
import acs.logic.UnauthorizedException;

@Service
public class DbElementService implements ExtendedElementService{
	private String projectName;
	private ElementDao elementDao;
	private ElementEntityConverter elementEntityConverter;
	private UserDao userDao;
	private AttributeConverter attributeConverter;
	
	@Autowired
	public DbElementService(ElementDao elementDao, ElementEntityConverter elementEntityConverter,
			UserDao userDao, AttributeConverter attributeConverter) {
		this.elementDao = elementDao;
		this.elementEntityConverter = elementEntityConverter;
		this.userDao = userDao;
		this.attributeConverter = attributeConverter;
	}

	// inject value from configuration or use default value
	@Value("${spring.application.name:2020b.daniel.zusev}") 
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	@PostConstruct
	public void init() {
		// initialize object after injection
		System.err.println("project name: " + this.projectName);
	}
	
	@Override
	@Transactional
	public ElementBoundary create(String managerDomain, String managerEmail, ElementBoundary element) {
		if(userDao.findById(managerDomain+"@@"+managerEmail).get().getRole().equals(UserRole.MANAGER)){
			String key = UUID.randomUUID().toString();
			if(element.getName() == null) {
				throw new RuntimeException("Element Name Can Not Be Null");
			}
			if(element.getType() == null) {
				throw new RuntimeException("Element Type Can Not Be Null");
			}
			if(element.getLocation() == null) {
				throw new RuntimeException("Element Location Can Not Be Null");
			}
			
			ElementEntity entity = this.elementEntityConverter.toEntity(element);
			entity.setElementId(this.projectName + "@@" + key);
			entity.setCreatedTimestamp(new Date());
			entity.setCreatedBy(managerDomain + "@@" + managerEmail);
			
//			if(element.getType().equals("Garden")) { 
//				GardenInfo info = new GardenInfo();
//				entity.getElementAttributes().put("Info", info);
//			}
//			if(element.getType().equals("Facility")) {//TODO insert information into Garden & Facility *****************
//				InfoFacility info = new InfoFacility();
//				entity.getElementAttributes().put("Info", info);
//			}
			
			return this.elementEntityConverter.fromEntity(this.elementDao.save(entity));
		}
		else {
			throw new UnauthorizedException("just manager can create an element");
		}
	}

	@Override
	@Transactional
	public ElementBoundary update(String managerDomain, String managerEmail, String elementDomain, String elementId,ElementBoundary update) {
		
		if(userDao.findById(managerDomain+"@@"+managerEmail).get().getRole().equals(UserRole.MANAGER)) {
			ElementEntity existing = this.elementDao.findById(elementDomain + "@@" + elementId)
					.orElseThrow(()->new NotFoundException("No element for id: " + elementId));
//					this.getSpecificElement(managerDomain, managerEmail, elementDomain, elementId);

			
			if(update.getType() != null) {
				existing.setType(update.getType());
			}
			
			if(update.getName() != null) {
				existing.setName(update.getName());
			}
			
			if(update.getActive() != null) {
				existing.setActive(update.getActive());
			}
			
			if(update.getLocation() != null) {
				LocationEntity location = 
						new LocationEntity(update.getLocation().getLat(), update.getLocation().getLng());
				existing.setLocation(location);
			}
			
			if(update.getElementAttributes() != null) {
				existing.setElementAttributes(update.getElementAttributes());
			}
			
			//Load lazy fields
			if(existing.getOrigin() != null)
				existing.getOrigin().getActive();
			if(existing.getChildElements() != null)
				existing.getChildElements().size();
			
			this.elementDao.save(existing);
		return this.elementEntityConverter.fromEntity(existing);
		}
		else {
			throw new UnauthorizedException("just manager can update an element");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<ElementBoundary> getAll(String userDomain, String userEmail) {
		return StreamSupport.stream(
				this.elementDao.findAll().spliterator(), false)
				.map(this.elementEntityConverter::fromEntity)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public ElementBoundary getSpecificElement(String userDomain, String userEmail, String elementDomain,
			String elementId) {
		UserRole userRole1 = userDao.findById(userDomain+"@@"+userEmail).get().getRole();
		if(userRole1.equals(UserRole.PLAYER)) {
			
			return this.elementEntityConverter.fromEntity(this.elementDao.findByElementIdAndActive((elementDomain + "@@" + elementId),true)
					.orElseThrow(()->new NotFoundException("No element for id: " + elementId)));
		}
		else if(userRole1.equals(UserRole.MANAGER)){
			return this.elementEntityConverter.fromEntity(this.elementDao.findById(elementDomain + "@@" + elementId)
					.orElseThrow(()->new NotFoundException("No element for id: " + elementId)));
		}
		throw new UnauthorizedException("Only player/manager can get specific element");
	}
		

	@Override
	@Transactional
	public void deleteAllElements(String adminDomain, String adminEmail) {
		if(userDao.findById(adminDomain+"@@"+adminEmail).get().getRole().equals(UserRole.ADMIN)) {
			this.elementDao.deleteAll();
		}
		else
			throw new UnauthorizedException("Only admin can delete all elements");
	}

	@Override
	@Transactional
	public void bindExistingElementToAnExistingChildElement(String managerDomain,String managerEmail,String originElementDomain, String originElementId, ElementIdBoundary elementIdBoundary) {
		if(userDao.findById(managerDomain+"@@"+managerEmail).get().getRole().equals(UserRole.MANAGER)) {
			if (elementIdBoundary.getId() == null) {
				throw new NotFoundException("No Such ID In Database");
			}
			
			ElementEntity origin = this.elementDao.findById(originElementDomain + "@@" + originElementId)
									.orElseThrow(() -> new NotFoundException("No Element For Id: " + originElementId));
			
			ElementEntity child = this.elementDao.findById(elementIdBoundary.getDomain() + "@@" + elementIdBoundary.getId())
								.orElseThrow(() -> new NotFoundException("No Element For Id: " + elementIdBoundary.getId()));
			
			origin.addChildElement(child);
//			GardenInfo originInfo = attributeConverter.toAttribute(origin.getElementAttributes().get("Info"), GardenInfo.class);
//			InfoFacility childInfo = attributeConverter.toAttribute(child.getElementAttributes().get("Info"), InfoFacility.class);
//			originInfo.getFacilityTypes().put(childInfo.getType(),child.getElementId());
			this.elementDao.save(origin);
		}
		else
			throw new UnauthorizedException("just manager can bind elements");
	}

	@Override
	@Transactional(readOnly = true)
	public ElementBoundary[] getAllChildrenOfAnExistingElement(String userDomain,String userEmail,String originElementDomain,String originElementId) {
		
		ElementEntity origin = this.elementDao.findById(originElementDomain + "@@" + originElementId)
				.orElseThrow(() -> new NotFoundException("No Element For Id: " + originElementId));
		
		return origin
					.getChildElements()
					.stream()
					.map(this.elementEntityConverter :: fromEntity)
					.collect(Collectors.toList())
					.toArray(new ElementBoundary[0]);

	}

	@Override
	@Transactional(readOnly = true)
	public ElementBoundary[] getAnArrayWithElementParent(String userDomain,String userEmail,String originElementDomain,String originElementId) {
		// TODO If we make a "many to many" relationship
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ElementBoundary> getAll(String userDomain, String userEmail, int size, int page) {
		UserRole userRole = userDao.findById(userDomain+"@@"+userEmail).get().getRole();
		if(userRole.equals(UserRole.PLAYER)) {
			return elementDao
					.findAllByActive(true,PageRequest.of(page, size, Direction.ASC, "createdTimestamp","elementId"))
					.stream()
					.map(this.elementEntityConverter :: fromEntity)
					.collect(Collectors.toList());
		}
		else if(userRole.equals(UserRole.MANAGER)) {
				return elementDao
						.findAll(PageRequest.of(page, size, Direction.ASC, "createdTimestamp","elementId"))
						.getContent()
						.stream()
						.map(this.elementEntityConverter :: fromEntity)
						.collect(Collectors.toList());
		}
		throw new UnauthorizedException("Admin cannot get all elements");
	}

	@Override
	@Transactional(readOnly = true)
	public ElementBoundary[] getAllChildrenOfAnExistingElement(String userDomain, String userEmail,
			String elementDomain, String elementId, int size, int page) {
		UserRole userRole = userDao.findById(userDomain+"@@"+userEmail).get().getRole();
		if(userRole.equals(UserRole.PLAYER)) {
			return elementDao
					.findAllByOrigin_ElementIdAndActive(elementDomain+"@@"+elementId, true, PageRequest.of(page, size, Direction.ASC, "createdTimestamp","elementId"))
					.stream()
					.map(this.elementEntityConverter :: fromEntity)
					.collect(Collectors.toList()).toArray(new ElementBoundary[0]);
		}
		else if(userRole.equals(UserRole.MANAGER)){
			return elementDao
					.findAllByOrigin_ElementId(elementDomain+"@@"+elementId, PageRequest.of(page, size, Direction.ASC, "createdTimestamp","elementId"))
					.stream()
					.map(this.elementEntityConverter :: fromEntity)
					.collect(Collectors.toList()).toArray(new ElementBoundary[0]);
		}
		throw new UnauthorizedException("Admin cannot get all Children Of An Existing Element ");
	}

	@Override
	@Transactional(readOnly = true)
	public ElementBoundary[] getAnArrayWithElementParent(String userDomain, String userEmail, String elementDomain,
			String elementId, int size, int page) {
		UserRole userRole = userDao.findById(userDomain+"@@"+userEmail).get().getRole();
		if(userRole.equals(UserRole.PLAYER)) {
			return elementDao
					.findAllOriginByChildElements_ElementIdLikeAndActive(elementDomain+"@@"+elementId, true, PageRequest.of(page, size, Direction.ASC, "createdTimestamp","elementId"))
					.stream()
					.map(this.elementEntityConverter :: fromEntity)
					.collect(Collectors.toList()).toArray(new ElementBoundary[0]);
		}
		else if(userRole.equals(UserRole.MANAGER)){
			return elementDao
					.findAllOriginByChildElements_ElementIdLike(elementDomain+"@@"+elementId, PageRequest.of(page, size, Direction.ASC, "createdTimestamp","elementId"))
					.stream()
					.map(this.elementEntityConverter :: fromEntity)
					.collect(Collectors.toList()).toArray(new ElementBoundary[0]);
		}
		throw new UnauthorizedException("Admin cannot get An Array With Element Parent");
	}

	@Override
	@Transactional(readOnly = true)
	public ElementBoundary[] getAllByName(String userDomain, String userEmail, String name, int size, int page) {
		UserRole userRole = userDao.findById(userDomain+"@@"+userEmail).get().getRole();
		if(userRole.equals(UserRole.PLAYER)) {
			return elementDao
					.findAllByNameAndActive(name, true,  PageRequest.of(page, size, Direction.ASC, "createdTimestamp","elementId"))
					.stream()
					.map(this.elementEntityConverter :: fromEntity)
					.collect(Collectors.toList()).toArray(new ElementBoundary[0]);
		}
		else if(userRole.equals(UserRole.MANAGER)){
			return elementDao
					.findAllByName(name,  PageRequest.of(page, size, Direction.ASC, "createdTimestamp","elementId"))
					.stream()
					.map(this.elementEntityConverter :: fromEntity)
					.collect(Collectors.toList()).toArray(new ElementBoundary[0]);
		}
		throw new UnauthorizedException("Admin cannot get All By Name");
	}
		

	@Override
	@Transactional(readOnly = true)
	public ElementBoundary[] getAllByType(String userDomain, String userEmail, String type, int size, int page) {
		UserRole userRole = userDao.findById(userDomain+"@@"+userEmail).get().getRole();
		if(userRole.equals(UserRole.PLAYER)) {
			return elementDao
					.findAllByTypeAndActive(type, true,  PageRequest.of(page, size, Direction.ASC, "createdTimestamp","elementId"))
					.stream()
					.map(this.elementEntityConverter :: fromEntity)
					.collect(Collectors.toList()).toArray(new ElementBoundary[0]);
		}
		else if(userRole.equals(UserRole.MANAGER)){
			return elementDao
					.findAllByType(type,PageRequest.of(page, size, Direction.ASC, "createdTimestamp","elementId"))
					.stream()
					.map(this.elementEntityConverter :: fromEntity)
					.collect(Collectors.toList()).toArray(new ElementBoundary[0]);
		}
		throw new UnauthorizedException("Admin cannot get All By Name");
	}

	@Override
	@Transactional(readOnly = true)
	public ElementBoundary[] getAllByLocation(String userDomain, String userEmail, double lat, double lng,
			double distance, int size, int page) {
		
		
		double maxLat = lat +distance;
		
		double minLat = lat - distance;
		
		double maxLng = lng + distance;
		
		double minLng = lng - distance;
		
		
		
		
		if(userDao.findById(userDomain+"@@"+userEmail).get().getRole().equals(UserRole.PLAYER)) {
			return this.elementDao.findAllByLocation_LatBetweenAndLocation_LngBetweenAndActive(
					minLat,
					maxLat,
					minLng,
					maxLng,
					true,
					PageRequest.of(page, size, Direction.ASC, "createdTimestamp","elementId"))
					.stream()
					.map(this.elementEntityConverter :: fromEntity)
					.collect(Collectors.toList()).toArray(new ElementBoundary[0]);
		}
		else if(userDao.findById(userDomain+"@@"+userEmail).get().getRole().equals(UserRole.MANAGER)){
			return this.elementDao.findAllByLocation_LatBetweenAndLocation_LngBetween(
					minLat,
					maxLat,
					minLng,
					maxLng,
					PageRequest.of(page, size, Direction.ASC, "createdTimestamp","elementId"))
					.stream()
					.map(this.elementEntityConverter :: fromEntity)
					.collect(Collectors.toList()).toArray(new ElementBoundary[0]);
		}
		else {
			throw new UnauthorizedException("Admin cannot search by location");
		}
	}
}
