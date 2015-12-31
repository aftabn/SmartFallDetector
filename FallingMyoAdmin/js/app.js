var app = angular.module("fallingMyoApp", ["firebase"]);

app.factory("unauthorizedPatients", ["$firebaseArray",
  function ($firebaseArray) {
      var unauthorizedReferece = new Firebase("https://fallingmyo.firebaseIO.com/Unauthorized");
      return $firebaseArray(unauthorizedReferece);
  }
]);

app.factory("authorizedPatients", ["$firebaseArray",
    function ($firebaseArray) {
        var authorizedReference = new Firebase("https://fallingmyo.firebaseIO.com/Authorized");
        return $firebaseArray(authorizedReference);
    }
]);

app.controller("unauthorizedController", ["$scope", "unauthorizedPatients", "authorizedPatients",

    function ($scope, unauthorizedPatients, authorizedPatients) {
        $scope.unauthorizedPatients = unauthorizedPatients;

        $scope.authorizedPatients = authorizedPatients;

        $scope.deletePatient = function (patient) {
            $scope.unauthorizedPatients.$remove(patient);
        }

        $scope.authorizePatient = function (patient) {
            var accountID = $scope.accountID;
            var newRoomNumber = $scope.newRoomNumber;
            $scope.authorizedPatients.$add(patient);
            $scope.unauthorizedPatients.$remove(patient);
        }
    }
]);

app.controller("authorizedController", ["$scope", "unauthorizedPatients", "authorizedPatients",

    function ($scope, unauthorizedPatients, authorizedPatients) {
        $scope.unauthorizedPatients = unauthorizedPatients;

        $scope.authorizedPatients = authorizedPatients;

        $scope.editRoomIndices = new Array();

        $scope.editMyoID = new Array();

        $scope.editPatientRoomNumber = function (index) {
            $scope.editRoomIndices[$scope.editRoomIndices.length] = index;
        }

        $scope.submitNewRoomNumber = function (patient, index) {
            $scope.authorizedPatients.$save(patient);
            $scope.editRoomIndices.splice($scope.editRoomIndices.indexOf(index), 1);
        }

        $scope.emergencyHandled = function (patient) {
            patient.emergency = false;
            $scope.authorizedPatients.$save(patient);
        }

        $scope.assistanceHandled = function (patient) {
            patient.assistanceRequired = "NONE";
            $scope.authorizedPatients.$save(patient);
        }

        $scope.enable = function (patient) {
            patient.disable = false;
            $scope.authorizedPatients.$save(patient);
        }

        $scope.disable = function (patient) {
            patient.disable = true;
            $scope.authorizedPatients.$save(patient);
        }

        $scope.deleteMyoID = function (patient) {
            patient.macAddress = "";
            $scope.authorizedPatients.$save(patient);
        }
    }
]);