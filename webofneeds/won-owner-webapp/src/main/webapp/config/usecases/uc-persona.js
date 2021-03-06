import { details, mergeInEmptyDraft } from "../detail-definitions.js";
import won from "../../app/service/won.js";
import { Generator } from "sparqljs";

window.SparqlGenerator4dbg = Generator;

export const persona = {
  identifier: "persona",
  label: "Persona",
  icon: undefined, //No Icon For Persona UseCase (uses identicon)
  draft: {
    ...mergeInEmptyDraft({
      content: {
        type: [won.WON.PersonaCompacted],
        sockets: {
          "#chatSocket": won.CHAT.ChatSocketCompacted,
          "#reviewSocket": won.REVIEW.ReviewSocketCompacted,
          "#holderSocket": won.HOLD.HolderSocketCompacted,
          "#buddySocket": won.BUDDY.BuddySocketCompacted,
        },
      },
      seeks: {},
    }),
  },
  details: {
    personaName: { ...details.personaName, mandatory: true },
    description: { ...details.description },
    website: { ...details.website },
    images: { ...details.images },
  },
  seeksDetails: {},
};
