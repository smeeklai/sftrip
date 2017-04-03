<?php
class Alert_model extends CI_Model {

	function __construct() {
		parent::__construct();
	}

	function get_taxi_info($tripID) {
		$this -> db -> where('tripID', $tripID);
		$query2 = $this -> db -> get('trip');
		return $query2 -> result();
	}


	function get_location($tripID) {
		$this -> db -> where('tripID', $tripID);
		$query = $this -> db -> get('logInfo');
		return $query -> result();
	}
}
?>