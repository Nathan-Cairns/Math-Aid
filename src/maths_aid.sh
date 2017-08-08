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
  while true; do
    clear
    displayTitle
    displayMenu
    read -p"
    Enter a selection [l/p/d/c/q]: " instruction
    case $instruction in
      [Qq] )
        exit
      ;;
      [Cc] )
        clear
        makeCreation
      ;;
    esac

  done
}
###########################
#### List creations(l) ####
###########################

###################################
#### Create a new creation (c) ####
###################################
function makeCreation() {
  # Create creations dir
  mkdir -p creations/
  while true; do
    ## TODO allow user to retype text
    echo
    read -p "   Please name your creation or press \"q\" to exit: " name
    visualComponent=creations/"$name"_vComp.mp4
    audioComponent=creations/"$name"_aComp.wav
    case $name in
      [Qq] )
        break
        ;;
      * )

        if [ -e $visualComponent ]; then
          clear
          echo
          echo "   Creation already exists please pick another name"
        else
          createCreation $name $visualComponent
          echo
          # Ask user about the name they selected
          while true; do
            clear
            echo
            echo "   Creation created: name = $name, what do you wish to do next?"
            echo "      (r)ename your creation"
            echo "      (k)eep your creation as is and proceed to audio recording"
            echo "      (q)uit **WARNING** aborting now will erase your progress"
            read -p "   Enter a selction: " answer
          case $answer in
            [Rr] )
              rm $visualComponent
              makeCreation
              break 2
            ;;
            [kK] )
              echo
              createAudio $name $audioComponent
              break 2
            ;;
            [qQ] )
              deleteCreationComponents $visualComponent
              break 2
            ;;
            * )
              clear
              echo
              echo "    Invalid input please input a value from the list!"
              sleep 1
              clear
            ;;
          esac
        done
        fi
      ;;
    esac
  done
}

function createCreation () {
  #create the creation
  ffmpeg -f lavfi -i color=c=orange:s=320x240:d=3.0 -vf \
  "drawtext=fontfile=/path/to/font.ttf:fontsize=30: \
  fontcolor=green:x=(w-text_w)/2:y=(h-text_h)/2:text=$name" \
  -t 3 $visualComponent &> /dev/null
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
  ffmpeg -f alsa -i hw:0 -t 3 $audioComponent &> /dev/null
  clear

  while true; do
    # re record audio if necessary
    echo
    echo "    Audio successfully recorded... "
    echo
    echo "    What do you wish to do with your recording?"
    echo "      (l)isten to your recording"
    echo "      (r)edo your recording"
    echo "      (k)eep your recording"
    echo "      (q)uit **WARNING** aborting now will erase your progress"
    read -p "    Enter a selection: " answer
    case $answer in
      [Ll] )
        ffplay -autoexit $audioComponent &> /dev/null
        ;;
      [Rr] )
        rm $audioComponent
        createAudio
        break
      ;;
      [Kk])
        combineAudioAndVideo $visualComponent $audioComponent $name
        break
      ;;
      [Qq] )
        deleteCreationComponents $visualComponent $audioComponent
        break
      ;;
      * )
        clear
        echo
        echo "    Invalid input please input a value from the list!"
        sleep 1
        clear
      ;;

    esac
    clear
  done
}

function combineAudioAndVideo {
  ffmpeg -i $visualComponent -i $audioComponent -c:v copy -c:a aac \
   -strict experimental creations/$name.mp4 &> /dev/null
   deleteCreationComponents $visualComponent $audioComponent
}

function deleteCreationComponents {
  if [ -e $visualComponent ]; then
    rm $visualComponent
  fi
  if [ -e $audioComponent ]; then
    rm $audioComponent
  fi
}


# Code start
promptUser
