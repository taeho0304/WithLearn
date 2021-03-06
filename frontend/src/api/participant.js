/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
// 클릭한 participant
var PARTICIPANT_MAIN_CLASS = 'participant participant-main col-lg-4 col-md-6 col-12';
var PARTICIPANT_CLASS = 'participant col-lg-4 col-md-6 col-12';
var PARTICIPANT_PANEL_CLASS = 'participant participant-panel col-md-2'

/**
 * Creates a video element for a new participant
 *
 * @param {String} name  - the name of the new participant, to be used as tag
 *                        name of the video element.
 *                        The tag of the new element will be 'video<name>'
 * @param {Boolean} isSharing - 화면공유 체크용 파라미터
 * @return
 */
// eslint-disable-next-line no-unused-vars
function Participant(name, sendMessage) {
  this.name = name;
  var container = document.createElement('div');
  container.className = PARTICIPANT_CLASS;
  container.id = name;
  var span = document.createElement('span');
  span.className = 'participant-name'
  var video = document.createElement('video');
  // eslint-disable-next-line no-unused-vars
  var rtcPeer;
  var isSharing;

  container.appendChild(video);
  container.appendChild(span);
  container.onclick = switchContainerClass;
  document.getElementById('participants').appendChild(container);

  span.appendChild(document.createTextNode(name));

  video.id = 'video-' + name;
  video.autoplay = true;
  video.controls = false;

  this.getElement = function() {
    return container;
  }

  this.getVideoElement = function() {
    return video;
  }

  function switchContainerClass() {
    // 확대된 화면이 없었던 경우,
    if (container.className === PARTICIPANT_CLASS) {
      const elements = Array.prototype.slice.call(document.getElementsByClassName(PARTICIPANT_CLASS));
      elements.forEach(function(item) {
        item.className = PARTICIPANT_PANEL_CLASS;
      });
      container.className = PARTICIPANT_MAIN_CLASS;
    }
    // 확대된 화면이 있고, 패널 클래스를 클릭했을 경우
    else if (container.className === PARTICIPANT_PANEL_CLASS) {
      const elements = Array.prototype.slice.call(document.getElementsByClassName(PARTICIPANT_MAIN_CLASS));
      elements.forEach(function(item) {
        item.className = PARTICIPANT_PANEL_CLASS;
      });
      container.className = PARTICIPANT_MAIN_CLASS;
    }
    // 메인 클래스를 클릭했을 경우, 모든 클래스를 일반 참가자로 돌린다
    else {
      const elements = Array.prototype.slice.call(document.getElementsByClassName(PARTICIPANT_PANEL_CLASS));
      elements.forEach(function(item) {
        item.className = PARTICIPANT_CLASS;
      });
      container.className = PARTICIPANT_CLASS;
    }
  }

  function isPresentMainParticipant() {
    return ((document.getElementsByClassName(PARTICIPANT_MAIN_CLASS)).length != 0);
  }

  this.offerToReceiveVideo = function(error, offerSdp/*, wp*/){
    if (error) return console.error ('sdp offer error')
    // console.log('Invoking SDP offer callback function');
    var msg =  { id : 'receiveVideoFrom',
        sender : name,
        sdpOffer : offerSdp
      };
    sendMessage(msg);
  }

  this.onIceCandidate = function (candidate/*, wp*/) {
    // console.log('새로운 화상채팅 참가자: ' + JSON.stringify(candidate));

    var message = {
      id: 'onIceCandidate',
      candidate: candidate,
      name: name
    };
    sendMessage(message);
  }

  Object.defineProperty(this, 'rtcPeer', { writable: true});
  Object.defineProperty(this, 'isSharing', { writable: true});

  this.dispose = function() {
    console.log(this.name + ' 화상채팅 종료');
    this.rtcPeer.dispose();
    container.parentNode.removeChild(container);
  };
}

export {
  Participant
}
