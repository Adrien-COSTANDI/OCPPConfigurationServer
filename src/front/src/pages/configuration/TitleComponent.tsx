import React, { Dispatch, SetStateAction } from "react";
import { ErrorState, GlobalState } from "./GlobalState";
import { Grid, Input, Paper } from "@mui/material";

function TitleComponent(props: {
    globalState: GlobalState;
    setGlobalState: Dispatch<SetStateAction<GlobalState>>,
    errorState: ErrorState
}) {
    const backgroundColor = props.errorState.name === "" ? '' : 'rgba(255, 0, 0, 0.2)'; // Replace with your desired colors

    return (
        <Paper elevation={2} sx={{p: 2, pt: 0, mt: 3, backgroundColor}}>
            <Grid direction={"column"} container justifyContent="space-between">
                <Grid xs={4} item>
                    <h4>Titre de la configuration : </h4>
                </Grid>
                <Grid xs={7} item>
                    <Input
                        onChange={event => {
                            props.setGlobalState(prevState => {
                                return {
                                    configuration: prevState.configuration,
                                    firmware: prevState.firmware,
                                    description: prevState.description,
                                    name: event.target.value
                                }
                            })
                        }}
                        fullWidth={true}
                        placeholder="Titre"/>
                </Grid>
                {props.errorState.name !== "" &&
                    <p>{props.errorState.name}</p>
                }
            </Grid>
        </Paper>
    )
}

export default TitleComponent;