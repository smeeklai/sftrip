<?php
  class sms_model extends CI_Model {
  	
	function __construct() {
		parent::__construct();
	}
	  	
	function findRelativeNO($email){
		$this->db->where('email', $email);
		$query = $this->db->get('personal_information');
		return $query -> result();
	}
	
	function getLogInfo($tripID) {
		$this->db->where('tripID', $tripID);
		$query = $this->db->get('logInfo');
		return $query -> result();
	}
  }
?>