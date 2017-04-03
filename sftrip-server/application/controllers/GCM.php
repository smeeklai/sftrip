<?php
class GCM extends CI_Controller {
	public function __construct() {
		parent::__construct();
		/*
		 load you helper library
		 */
		/*
		 load you model
		 */
		$this -> load -> model('gcm_model');
	}

	function index() {
		//$email = $this -> input -> post("email");
		$email = "boss";
		$query = $this -> gcm_model -> getUserGCMId($email);
		if ($query == null) {
			$arr['success'] = "0";
			$arr['error'] = "1";
			$arr['error_msg'] = "Something is wrong with Server. Please try again";
		} else {
			$arr['success'] = "1";
			$arr['error'] = "0";
			$arr['userGCMId'] = $query[0]->gcm_id;
		}

		echo json_encode($arr);
	}

}
?>