import { Box, Button, Grid } from "@mui/material";
import FormInput from "../../../sharedComponents/FormInput";
import RoleComponent from "./components/RoleComponent";
import { useEffect, useState } from "react";
import { Role, createNewUser } from "../../../conf/userController";


function AddAccount() {
    const [lastName, setLastName] = useState("");
    const [firstName, setFirstName] = useState("");
    const [mail, setMail] = useState("");
    const [role, setRole] = useState<Role>(Role.VISUALIZER);
    const [isButtonDisabled, setIsButtonDisabled] = useState(true);
    const [password1, setPassword1] = useState("");
    const [password2, setPassword2]= useState("");
    const [toast, setToast] = useState(false); // A vier si toast sûrement
    const [display, setDisplay] = useState(false); // A vier si toast sûrement

    useEffect(() => {
        if (
            password1 === password2
            && password1 !== ""
        ) {
            setIsButtonDisabled(false);
        } else {
            setIsButtonDisabled(true);
        }
    }, [password1, password2]);

    return (
        <Grid container sx={{alignContent: "center", width: "100%"}}>
            <Grid item xs={12} sx={{ml: "35%", mr: "35%"}}>
                <FormInput name={"Nom"}
                    onChange={lastName => setLastName(lastName)}
                />
            </Grid>
            <Grid item xs={12} sx={{ml: "35%", mr: "35%"}}>
                <FormInput name={"Prénom"}
                    onChange={firstName => setFirstName(firstName)}
                />
            </Grid>
            <Grid item xs={12} sx={{ml: "35%", mr: "35%"}}>
                <FormInput name={"Email"}
                    onChange={mail => setMail(mail)}
                />
            </Grid>
            <Grid item xs={12} sx={{ml: "35%", mr: "35%"}}>
                <FormInput name={"Mot de passe"}
                    onChange={password => setPassword1(password)}
                    isPassword={true}
                />
            </Grid>
            <Grid item xs={12} sx={{ml: "35%", mr: "35%"}}>
                <FormInput name={"Confirmer le mot de passe"}
                    onChange={password => setPassword2(password)}
                    isPassword={true}
                />
            </Grid>
            <Grid item xs={12} sx={{ml: "35%", mr: "35%"}}>
                <RoleComponent role={role} setRole={setRole}/>
            </Grid>
            <Grid item xs={12} sx={{ml: "35%", mr: "35%", mt: "1%", textAlign: "center"}}>
            <Button 
                sx={{borderRadius: 28}} 
                variant="contained" 
                color="primary"
                disabled={isButtonDisabled}
                onClick={() => {
                        const user = {
                            firstName: firstName,
                            lastName: lastName, 
                            email: mail, 
                            password: password1, 
                            role: role
                        }
                        if (user !== undefined) {
                            let returnValue = createNewUser(user)
                            returnValue.then(value => {
                                console.log("value : " + value)
                                setToast(value)
                                setDisplay(true)
                            })
                            console.log("toast : " + toast)
                        }
                    }
                }
            >
                Créer
            </Button>
            </Grid>
            <Grid item xs={12} sx={{ml: "35%", mr: "35%", mt: "1%", textAlign: "center"}}>
                <Box>
                    {toast && display && (
                        // TODO : Mettre le toast ici
                        <div style={{color: "green"}}>L'utilisateur a été créé.</div>
                        ) 
                    }
                    {!toast &&  display && (
                        // TODO : Mettre le toast ici 
                        <div style={{color: "red"}}>L'utilisateur existe déjà.</div>
                        )                         
                    }
                </Box>
            </Grid>
        </Grid>
    )
}

export default AddAccount;