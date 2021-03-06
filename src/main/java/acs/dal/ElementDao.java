package acs.dal;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import acs.data.ElementEntity;

public interface ElementDao extends PagingAndSortingRepository<ElementEntity, String>{
	
	
	public List<ElementEntity> findAllByType(
			@Param("type") String expectType,
			Pageable pageable);
	
	public List<ElementEntity> findAllByTypeAndActive(
			@Param("type") String expectType,
			@Param("active") Boolean expectActive,
			Pageable pageable);
	
	public List<ElementEntity> findAllByName(
			@Param("name") String expectname,
			Pageable pageable);
	
	public List<ElementEntity> findAllByNameAndActive(
			@Param("name") String expectname,
			@Param("active") Boolean expectActive,
			Pageable pageable);
	
	public List<ElementEntity> findAllByLocation(
			@Param("location") String expectLocation,
			Pageable pageable);
	
	public List<ElementEntity> findAllByLocationAndActive(
			@Param("location") String expectLocation,
			@Param("active") Boolean expectActive,
			Pageable pageable);
	
	public List<ElementEntity> findAllByOrigin_ElementId(
			@Param("originElemetId") String expectOrigin,
			Pageable pageable);
	
	public List<ElementEntity> findAllByOrigin_ElementIdAndActive(
			@Param("originElemetId") String expectOrigin,
			@Param("active") Boolean expectActive,
			Pageable pageable);
	
	public List<ElementEntity> findAllOriginByChildElements_ElementIdLike(
			@Param("elementId") String expectChildElementId,
			Pageable pageable);
	
	public List<ElementEntity> findAllOriginByChildElements_ElementIdLikeAndActive(
			@Param("childElements") String expectChildElement,
			@Param("active") Boolean expectActive,
			Pageable pageable);
	
	public List<ElementEntity> findAllByActive(
			@Param("active") Boolean expectActive,
			Pageable pageable);
	
	public Optional <ElementEntity> findByElementIdAndActive(
			@Param("elementId") String expectElementId,
			@Param("active") Boolean expectActive);
	
	public List<ElementEntity> findAllByLocation_LatBetweenAndLocation_LngBetweenAndActive(
			@Param("lat") double minLat,
			@Param("lat2") double maxLat,
			@Param("lng") double minLng,
			@Param("lng2") double maxLng,
			@Param("active") boolean active,
			Pageable pageable);
		
	public List<ElementEntity> findAllByLocation_LatBetweenAndLocation_LngBetween(
			@Param("lat") double minLat,
			@Param("lat2") double maxLat,
			@Param("lng") double minLng,
			@Param("lng2") double maxLng,
			Pageable pageable);
		
}
