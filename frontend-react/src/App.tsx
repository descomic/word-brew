import React, {useEffect, useState} from 'react'
import './App.css'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

const SERVER_ADDRESS = 'http://localhost:8080/ws-endpoint'

function App() {
    return (
        <div className="App">
            <title>Not Semantle</title>
            <div className='App-body'>
                <h1>
                    Not Semantle, a rework of the classic game.
                </h1>
                <GameState/>
            </div>
        </div>
    )
}

function SubmitArea(props: { submitGuess: (guess: string) => void }) {
    let [guess, setGuess] = useState<string>('')
    return (
        <form onSubmit={(event) => {
            event.preventDefault()
            props.submitGuess(guess)
            setGuess('')
        }}>
            <input value={guess} onChange={(event) => setGuess(event.target.value)}/>
            <button>Submit</button>
        </form>
    )
}

function GameState() {
    let [guesses, setGuesses] = useState<Map<string, number>>(new Map())
    const socket = new SockJS(SERVER_ADDRESS)
    const stompClient = Stomp.over(socket)

    useEffect(() => {
        if (!stompClient.connected) {
            stompClient.connect(
                {},
                (frame) => {
                    console.log('Subscribing to /topic/guesses  ')
                    stompClient.subscribe('/topic/guesses', (message) => {
                        console.log('Received: ' + message)
                        message.ack()
                        const messageBody = JSON.parse(message.body)
                        const newGuesses = new Map<string, number>(Object.entries(messageBody))
                        console.log('Message body: ' + newGuesses)
                        setGuesses(newGuesses)
                    })
                },
                (error) => console.log('Error: ' + error)
            )
        }
        return () => {
            if (stompClient.connected) {
                stompClient.disconnect(() => {
                    console.log('Disconnected')
                })
            }
        }
    }, [stompClient])

    return (
        <div>
            <SubmitArea
                submitGuess={guess => stompClient.send("/app/submit", {}, guess)}/>
            {/*<progress value={Math.max(...guesses.map(guess => guess.score))}/>*/}
            <table>
                <thead>
                <tr>
                    <th>Guess</th>
                    <th>Score</th>
                </tr>
                </thead>
                <tbody>
                {
                    Array.from(guesses.entries())
                        .sort((a, b) => b[1] - a[1])
                        .map(([guess, score], index) => {
                            return (
                                <tr key={index}>
                                    <td>{guess}</td>
                                    <td>{Math.floor(score * 1000)}</td>
                                    <td>{<progress value={score}/>}</td>
                                </tr>
                            )
                        })
                }
                </tbody>
            </table>

        </div>
    )
}


export default App
