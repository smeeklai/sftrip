<?php
class LogInfo_model extends CI_Model {
	function __construct() {
		parent::__construct();

		$this -> load -> helper('date');
	}

	function create_logInfo($tripID, $lat, $long, $remainingDistance, $CRMP, $status) {
		$datestring = "%Y-%m-%d %H:%i:%s";
		$new_logInfo = array('tripID' => $tripID, 'datetime' => mdate($datestring), 'latitude' => $lat, 
		'longtitude' => $long, 'remainingDistance' => $remainingDistance, 'closetRemainingDistancePoint' => $CRMP,
		'arrived' => $status);
		$this -> db -> insert('logInfo', $new_logInfo);
		$id = $this -> db -> insert_id();
		return $id;
	}

	function check_logInfo($tripID, $index) {
		$this -> db -> where('tripID', $tripID);
		$this -> db -> where('index', $index);
		$query = $this -> db -> get('logInfo');
		if ($query -> num_rows >= 1) {
			return true;
		}
	}

	function getDestinationLatitude($tripID) {
		$this -> db -> where('tripID', $tripID);
		$this -> db -> select('desLat');
		$query = $this -> db -> get('trip');
		$result = $query -> result();
		return $result[0] -> desLat;
	}

	function getDestinationLongtitude($tripID) {
		$this -> db -> where('tripID', $tripID);
		$this -> db -> select('desLng');
		$query = $this -> db -> get('trip');
		$result = $query -> result();
		return $result[0] -> desLng;
	}

	function getClosetRemainingDistance($index) {
		$this -> db -> where('index', $index);
		$this -> db -> select('remainingDistance');
		$query = $this -> db -> get('logInfo');
		if ($query -> num_rows > 0) {
			$result = $query -> last_row('array');
			//$result = $row -> result();
			return $result['remainingDistance'];
		} else {
			return null;
		}
	}
	
	function getIndexOfLastRow($tripID) {
		$this -> db -> where('tripID', $tripID);
		$this -> db -> select('index');
		$query = $this -> db -> get('logInfo');
		if ($query -> num_rows > 0) {
			$result = $query -> last_row('array');
			//$result = $row -> result();
			return $result['index'];
		} else {
			return null;
		}
	}
	
	function getRemainingDistance($tripID) {
		$this -> db -> where('tripID', $tripID);
		$this -> db -> select('remainingDistance');
		$query = $this -> db -> get('logInfo');
		if ($query -> num_rows >= 4) {
			$result = $query->result();
			$result_array = array($result[sizeof($result) - 2] -> remainingDistance,
			$result[sizeof($result) - 3] -> remainingDistance,
			$result[sizeof($result) - 4] -> remainingDistance);
			return $result_array;
		} else {
			return null;
			/*$result = $query->result();
			$size = sizeof($result);
			$result_array = array();
			for($i = $size - 1; $i >= 0; $i--) {
				array_push($result_array, $result[$i] -> remainingDistance);
			}
			return $result_array;*/
		}
		
	}
	
	function getIndexOfCRD($tripID) {
		$this->db->where('tripID', $tripID);
		$this->db->where('closetRemainingDistancePoint', 1);
		$this->db->select('index');
		$query = $this->db->get('logInfo');
		if ($query -> num_rows > 0) {
			$result = $query -> last_row('array');
			return $result['index'];
		} else {
			return null;
		}
	}
	
	function setCurrentCRDPToOld($index) {
		$data = array('closetRemainingDistancePoint' => 0);
		$this->db->where('index', $index);
		$this->db->update('logInfo', $data);
	}
	
	function setRowToBeCRD($index) {
		$data = array('closetRemainingDistancePoint' => 1);
		$this->db->where('index', $index);
		$this->db->update('logInfo', $data);
		return true;
	}

	function getArrivedStatus($index) {
		$this -> db -> where('index', $index);
		$this -> db -> select('arrived');
		$query = $this -> db -> get('logInfo');
		return $query -> result();
	}

	function isTripExisted($tripID) {
		$this -> db -> where('$tripID', $tripID);
		$query = $this -> db -> get('logInfo');
		if ($query -> num_rows >= 1) {
			return true;
		}
	}

}
?>