<?php
class CheckLogin extends CI_Controller {
	public function __construct() {
		parent::__construct();
		/*
		 load you helper library
		 */
		/*
		 load you model
		 */
		$this -> load -> model('login_model');
	}

	function index() {
		$strUsername = $this -> input -> post("email");
		$strPassword = $this -> input -> post("password");

		$query = $this -> login_model -> validate_email_password($strUsername, $strPassword);
		if ($query) {
			$arr['success'] = "1";
			$arr['error'] = "0";
			$arr['error_msg'] = "";
		} else {
			$arr['success'] = "0";
			$arr['error'] = "1";
			$arr['error_msg'] = "Incorrect Username and Password";
		}

		echo json_encode($arr);
	}

}
?>