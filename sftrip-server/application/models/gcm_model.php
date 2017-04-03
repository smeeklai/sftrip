<?php
class Gcm_model extends CI_Model {
	function __construct() {
		parent::__construct();
	}
	
	function getUserGCMId($email) {
		$this->db->where('email', $email);
		$this->db->select('gcm_id');
		$query = $this->db->get('personal_information');
		if ($query -> num_rows == 1) {
			return $query -> result();
		} else {
			return null;
		}
	}
}
?>