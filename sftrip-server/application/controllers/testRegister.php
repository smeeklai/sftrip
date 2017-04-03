<?php
class TestRegister extends CI_Controller {
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
		
		$userEmail = "boss2";
		$userPass = "boss2";
		$userFirstName = "boss2";
		$userLastName = "boss2";
		$userMobile = "boss2";
		$userRM1 = "boss2";
		$userRM2 = "boss2";
		$userRM3 = "boss2";
		
		/*$userEmail = $this -> input -> post("email");
		$userPass = $this -> input -> post("password");
		$userFirstName = $this -> input -> post("firstname");
		$userLastName = $this -> input -> post("lastname");
		$userMobile = $this -> input -> post("mobile");
		$userRM1 = $this -> input -> post("relativemobile1");
		$userRM2 = $this -> input -> post("relativemobile2");
		$userRM3 = $this -> input -> post("relativemobile3");*/

		$this -> login_model -> create_member($userEmail, $userPass, $userFirstName, $userLastName, $userMobile, $userRM1, $userRM2, $userRM3);

		$query = $this -> login_model -> check_email($userEmail);
		if ($query) {
			$arr['success'] = "1";
			$arr['error'] = "0";
			$arr['error_msg'] = "";
		} else {
			$arr['success'] = "0";
			$arr['error'] = "1";
			$arr['error_msg'] = "Something wrong with your registration";
		}
		echo json_encode($arr);
	}
}
?>