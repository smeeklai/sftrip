<?php
class Login_model extends CI_Model {
	function __construct() {
		parent::__construct();
	}

	function create_member($email, $password, $firstname, $lastname, $mobile, $relativemobile1,
	$relativemobile2, $relativemobile3) {
		$new_member_insert_data = array('email' => $email, 'password' => $password, 'firstname' => $firstname,
		'lastname' => $lastname, 'mobile' => $mobile, 'relativemobile1' => $relativemobile1,
		'relativemobile2' => $relativemobile2, 'relativemobile3' => $relativemobile3);
		$insert = $this->db->insert('personal_information', $new_member_insert_data);
	}
	
	function validate_email_password($email, $password) {
		$this -> db -> where('email', $email);
		$this -> db -> where('password', $password);
		$qeury = $this -> db -> get('personal_information');
		if ($qeury -> num_rows == 1) {
			return true;
		}
	}
	
	function check_email($email) {
		$this->db->where('email', $email);
		$qeury = $this -> db -> get('personal_information');
		if($qeury -> num_rows == 1) {
			return true;
		}
	}
	
	function get($email) {
		return $email;
	}
}
?>