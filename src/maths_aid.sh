#! /bin/bash

######################################
#### Deals with displaying things ####
######################################
function displayTitle() {
  echo "
     __  __       _   _                    _     _
    |  \/  |     | | | |             /\   (_)   | |
    | \  / | __ _| |_| |__  ___     /  \   _  __| |
    | |\/| |/ _  | __| '_ \/ __|   / /\ \ | |/ _  |
    | |  | | (_| | |_| | | \__ \  / ____ \| | (_| |
    |_|  |_|\__,_|\__|_| |_|___/ /_/    \_\_|\__,_| "
}
function displayMenu() {
  echo "  ==============================================================
  Welcome to the Maths Authoring Aid
  =============================================================="
  echo
  echo "    Please select from one of the following options

    (l)ist existing creations
    (p)lay an existing creation
    (d)elete an existing creation
    (c)reate a new creation
    (q)uit authoring tool"
}
function promptUser() {
    clear
    displayTitle
    displayMenu
    echo
    read -n 1 -s -r -p "    Enter a selection [l/p/d/c/q]: " instruction
    case $instruction in
      [Qq] )
        clear
        echo
        read -n 1 -s -r -p "   Are you sure you want to quit? [y/n]: " reply
        if [ $reply == y ] || [ $reply == Y ]; then
          clear
          exit
        fi
      ;;
      [Cc] )
        makeCreation
      ;;
      [Ll] )
        listCreations
      ;;
      [Pp] )
        playCreation
      ;;
      [Dd] )
        deleteCreation
      ;;
      * )
        clear
        echo
        echo "    Invalid input please input a value from the list!"
        sleep 1
        clear
      ;;
    esac
    promptUser
}

###########################
#### List creations(l) ####
###########################

# Assigns a list of creation names to an array it is parsed.
function updateArrayOfCreations {
  creationList=()
  i=1
  for file in *.mp4 ; do
    creationList[i]="$file"
    i=$(($i+1))
  done
}

function displayCreationList {
  echo
  echo "    Existing creations: "
  echo "======================================"
  i=1
  for creation in "${creationList[@]}" ; do
    echo "      ($i) $(basename "$creation"| cut -f 1 -d '.')"
    i=$(($i+1))
  done
  echo "======================================"
}

# Used to listCreations
function listCreations {
  clear
  pushd "$DIR"/creations &> /dev/null

  if [ "$(ls | grep mp4$ | wc -l)" -gt 0 ]; then
    updateArrayOfCreations
    displayCreationList
    echo
    read -n 1 -s -r -p "    Press any key to exit back to main menu: "
  else
    echo
    echo "    There are no creations :("
    sleep 1
  fi
  popd
}

#######################################
#### Play an existing creation (p) ####
#######################################

function playCreation {
  clear

  pushd "$DIR"/creations &> /dev/null

  if [ "$(ls | grep mp4$ | wc -l)" -gt 0 ]; then
    updateArrayOfCreations
    displayCreationList
    echo
    read -p "   Enter the number of the creation you wish to Play or q to quit: " i
    if [ $i -le ${#creationList[@]} ] && [ $i -gt 0 ] && [ $i -eq $i 2>/dev/null ]; then
      clear
      echo
      echo "    Playing creation: $(basename "${creationList[${i}]}"| cut -f 1 -d '.')"
      ffplay -autoexit "${creationList[${i}]}" &> /dev/null
      playCreation
    elif [ $i == q ] || [ $i == Q ]; then
      echo
    else
      clear
      echo
      echo "    Please input a number from the list!"
      sleep 1
      playCreation
  fi
    else
    echo
    echo "    There are no creations :("
    sleep 1
  fi

  popd
}

###############################
#### Delete a creation (d) ####
###############################
function deleteCreation {
  clear

  pushd "$DIR"/creations &> /dev/null
  if [ "$(ls | grep mp4$ | wc -l)" -gt 0 ]; then
  updateArrayOfCreations
  displayCreationList

  echo
  read -p "   Enter the number of the creation you wish to delete or q to quit: " i

  if [ $i -le ${#creationList[@]} ] && [ $i -gt 0 ] && [ $i -eq $i 2>/dev/null ]; then
    clear
    echo
    read -n 1 -s -r -p "    Are you sure you want to delete $(basename "${creationList[${i}]}"| cut -f 1 -d '.')? [y/n]: " reply
    case $reply in
      [Yy] )
        rm "${creationList[${i}]}"
        echo
        echo "    $(basename "${creationList[${i}]}"| cut -f 1 -d '.') deleted"
        sleep 1
      ;;
      * )
        echo
        echo "    deletion aborted"
        sleep 1
      ;;
    esac
    deleteCreation
  elif [ $i == q ] || [ $i == Q ]; then
      echo
  else
    clear
    echo
    echo "    Please input a number from the list!"
    sleep 1
    deleteCreation
  fi
else
echo
echo "    There are no creations :("
sleep 1
fi

  popd
}


###################################
#### Create a new creation (c) ####
###################################
function creationNameMenu {
  # Ask user about the name they selected
    clear
    echo
    echo "   Creation created: name = "$name", what do you wish to do next?"
    echo "      (r)ename your creation"
    echo "      (k)eep your creation as is and proceed to audio recording"
    echo "      (q)uit **WARNING** aborting now will erase your progress"
    read -n 1 -s -r -p "   Enter a selction: " answer
  case $answer in
    [Rr] )
      rm "$visualComponent"
      makeCreation
    ;;
    [kK] )
      echo
      createAudio "$name" "$audioComponent"
    ;;
    [qQ] )
      clear
      echo
      read -n 1 -s -r -p "    Are you sure you want to abort? Progress will be lost? [y/n]: " reply
      case $reply in
        [Yy] )
          deleteCreationComponents "$visualComponent"
        ;;
        * )
        creationNameMenu
        ;;
      esac
    ;;
    * )
      clear
      echo
      echo "    Invalid input please input a value from the list!"
      sleep 1
      creationNameMenu
    ;;
  esac
}

## TODO ask user if they are sure they want to exit!!!!!!
function makeCreation() {
  mkdir -p "$DIR"/creations/
  clear

    echo
    read -p "   Please name your creation: " name
    visualComponent="$DIR"/creations/"$name"_vComp.mp4
    audioComponent="$DIR"/creations/"$name"_aComp.wav
        if [ -e "$DIR"/creations/"$name".mp4 ]; then
          clear
          echo
          echo "   Creation already exists please pick another name"
          sleep 1
          makeCreation
        else
          #create the creation
          ffmpeg -f lavfi -i color=c=orange:s=320x240:d=3.0 -vf \
          "drawtext=fontfile=/path/to/font.ttf:fontsize=30: \
          fontcolor=green:x=(w-text_w)/2:y=(h-text_h)/2:text=$name" \
          -t 3 "$visualComponent" &> /dev/null

          creationNameMenu $name $visualComponent $audioComponent

          # Check if creation was succesfully made and as the user if they wish to review it
          if [ -e "$DIR"/creations/"$name".mp4 ]; then
            reviewCreation
          fi
        fi
}

function reviewCreation {
  clear
  echo
  read -n 1 -s -r -p "   Do you wish to review your creation before being returned to the main menu? [y/n]: " reply
  case $reply in
    [Yy] )
      ffplay -autoexit "$DIR"/creations/"$name".mp4 &> /dev/null
    ;;
    * )

    ;;
  esac
}

function creationAudioMenu {
  clear
  echo
  echo "    Audio successfully recorded... "
  echo
  echo "    What do you wish to do with your recording?"
  echo "      (l)isten to your recording"
  echo "      (r)edo your recording"
  echo "      (k)eep your recording and finish making creation"
  echo "      (q)uit **WARNING** aborting now will erase your progress"
  read -n 1 -s -r -p "    Enter a selection: " answer
  case $answer in
    [Ll] )
      ffplay -autoexit "$audioComponent" &> /dev/null
      creationAudioMenu
      ;;
    [Rr] )
      rm "$audioComponent"
      createAudio
    ;;
    [Kk])
      combineAudioAndVideo "$visualComponent" "$audioComponent" "$name"
      clear
      echo
      echo "    Creation successfully created!"
      sleep 1
    ;;
    [Qq] )
      clear
      echo
      read -n 1 -s -r -p "    Are you sure you want to exit? Progress will be lost [y/n]: " reply
      case $reply in
        [Yy] )
          deleteCreationComponents "$visualComponent" "$audioComponent"
        ;;
        * )
          creationAudioMenu
        ;;
      esac
    ;;
    * )
      clear
      echo
      echo "    Invalid input please input a value from the list!"
      sleep 1
      clear
      creationAudioMenu
    ;;

  esac
}

function createAudio {
  clear
  echo
  read -n 1 -s -r -p "   Press any key to start recording: "
  echo
#  echo "    Recording in 3..."
#  sleep 1
#  echo "    Recording in 2..."
#  sleep 1
#  echo "    Recording in 1..."
#  sleep 1
  echo "   Recording"

  # Create the audioComponent
  ffmpeg -f alsa -i hw:0 -t 3 "$audioComponent" &> /dev/null
  clear

  creationAudioMenu $audioComponent
}

function combineAudioAndVideo {
  ffmpeg -i "$visualComponent" -i "$audioComponent" -c:v copy -c:a aac \
   -strict experimental "$DIR"/creations/"$name".mp4 &> /dev/null
   deleteCreationComponents "$visualComponent" "$audioComponent"
}

function deleteCreationComponents {
  if [ -e "$visualComponent" ]; then
    rm "$visualComponent"
  fi
  if [ -e "$audioComponent" ]; then
    rm "$audioComponent"
  fi
}


# Code start
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
mkdir -p "$DIR"/creations/
declare -a creationList

promptUser
