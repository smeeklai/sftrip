<?php
class Place_model extends CI_Model {
	function __construct() {
		parent::__construct();
	}
	
	function add_favorite_place($email, $placeID, $placeName, $lat, $long) {
		$new_place_data = array('email' => $email, 'placeID' => $placeID, 'placeName' => $placeName,
		'latitude' => $lat, 'longtitude' => $long);
		
		$this->db->insert('places', $new_place_data);
	}
	
	function update_favorite_place($email, $placeID, $placeName, $lat, $long) {
		$data = array('email' => $email, 'placeID' => $placeID, 'placeName' => $placeName, 'latitude' => $lat,
		'longtitude' => $long);	
		$this->db->where('email', $email);
		$this->db->where('placeID', $placeID);
		$result = $this->db->update('places', $data);
		return $result;
	}
	
	function is_enable_to_insert($email, $placeID) {
		$this->db->where('email', $email);
		$this->db->where('placeID', $placeID);
		$query = $this->db->get('places');
		if ($query -> num_rows == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	function check_favorite_place($email, $placeID) {
		$this->db->where('email', $email);
		$this->db->where('placeID', $placeID);
		$query = $this->db->get('places');
		if ($query -> num_rows == 1) {
			return true;
		}
	}
	
	function getLatitude($placeID, $email) {
		$this->db->select('latitude');
		$this->db->where('placeID', $placeID);
		$this->db->where('email', $email);
		$query = $this->db->get('places');
		return $query->result();
	}
	
	function getLongtitude($placeID, $email) {
		$this->db->select('longtitude');
		$this->db->where('placeID', $placeID);
		$this->db->where('email', $email);
		$query = $this->db->get('places');
		return $query->result();
	}
	
	function getInformation($email, $placeID) {
		$this->db->where('placeID', $placeID);
		$this->db->where('email', $email);
		$query = $this->db->get('places');
		if ($query->num_rows == 1) {
			return $query->result();	
		} else {
			return null;
		}
	}
	
	
}
?>