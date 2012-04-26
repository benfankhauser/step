/**
 * The Login object is able to show/hide the login popup, register, etc.
 */
function Login() {
	this.root = $("#login");
	var self = this;

	//hide the register popup by default
	$("#registerPopup", this.root).hide();

	
	this.root.hear("show-login-popup", function(selfElement, data) {
		if(data) {
			self.showLoginPopup(data.message, data.callback);
		} else {
			self.showLoginPopup();
		}
	});

	this.root.hear("show-register-popup", function(selfElement, data) {
		self.showLoginPopup();
	});
	
	//we check whether we are logged on already...
	$.getSafe(USER_GET_LOGGED_IN_USER, function(data) {
		//if we have data, we use it to show it
		if(data && data.name) {
			//set the name of the user
			self.setLoggedInUser(data.name);
		}
	});
}

/**
 * shows the login popup
 * @param title the title of the login popup
 * @param the event to fire if we log on successfully
 */
Login.prototype.showLoginPopup = function(popupTitle, callback) {
	var self = this;
	this.registerMode = false;
	
	$("#loginPopup").toggle(true);
	$("#registerPopup", this.root).slideUp();
	this.root.dialog({
		buttons : { 
			"Login" : function() {
				//send info to backend
				var email = $("#emailAddress", self.root).val();
				var password = $("#password", self.root).val();
				
				var popup = this;
				//TODO add validation
				//TODO enhance error handling to be central
				$.getSafe(USER_LOGIN + email + "/" + password, function(data) {
					//we have logged in succesfully
					//change the name of the user
					self.performSuccessfulLogin(data, popup, callback);
				});
			},
			"Create an account" : function() {
				//show register popup
				self.showRegisterPopup(callback);
			},
			"Cancel" : function() {
				$(this).dialog("close");
			}
		},
		modal: true,
		title: popupTitle ? popupTitle : "Login as an existing user"
	});
};

/**
 * performs a succesful login by changing the name at the top right of the screen
 */
Login.prototype.performSuccessfulLogin = function(data, popup, callback) {
	this.setLoggedInUser(data.name);
	$(popup).dialog("close");
	
	if(callback) {
		callback();
	}
};

/**
 * shows the register popup
 */
Login.prototype.showRegisterPopup = function(callback) {
	this.registerMode = true;
	var self = this;
	this.root.dialog({
		buttons : { 
			"Register" : function() {
				//	send information to server
				var email = $("#emailAddress", self.root).val();
				var name = $("#name", self.root).val();
				var country = $("#country", self.root).val();
				var password = $("#password", self.root).val();
				var popup = this;
				
				$.getSafe(USER_REGISTER + 
						email + "/" +
						name + "/" +
						country + "/" +
						password, 
						function(data) {
							self.performSuccessfulLogin(data, popup, callback);
						}
				);
			},
			"Cancel" : function() {
				self.showLoginPopup();
			}
		},
		title: "Register as a new user"
	});
	$("#registerPopup", this.root).slideDown(1000);
};

/**
 * offers the option of logging the user out
 */
Login.prototype.showLogout = function() {
	//TODO fix height
	this.root.slideUp(0);
	$("#loginPopup").toggle(false);
	$("#registerPopup").toggle(false);
	
	var popup = this.root;
	this.root.dialog({
		buttons : {
			"Logout" : function() {
				//do logout
				$.getSafe(USER_LOGOUT, function(){
					var loginLink = $("#loginLink", this.root);
					loginLink.text("Login");
					loginLink.unbind('click');
					loginLink.click(function() {
						login();
					});
					popup.dialog("close");
					$.shout("user-logged-out");
				});
			},
			"Cancel" : function() {
				$(this).dialog("close");
			}
		}
	});
};

/**
 * sets the user's name up and rebinds the click to 
 * be a logout operation instead.
 */
Login.prototype.setLoggedInUser = function(name) {
	var loginLink = $("#loginLink");
	var self = this;
	
	loginLink.text(name);
	loginLink.unbind('click');
	$.shout("user-logged-in");
	loginLink.click(function() {
		self.showLogout();
	});
};
